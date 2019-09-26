package com.papco.sundar.papcojoballotment.transactions

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Transaction
import com.papco.sundar.papcojoballotment.documents.Place
import com.papco.sundar.papcojoballotment.documents.PrintJob

class RemoveFromQueueTransaction(jobToRemove: PrintJob, machineId:String)
  :Transaction.Function<Unit>{

  private val db=FirebaseFirestore.getInstance()
  private val poolReference=db.collection("places").document("pool")
  private lateinit var poolDocument:DocumentSnapshot
  private var pool: Place?=null

  private val machineReference=db.collection("places").document(machineId)
  private lateinit var machineDocument:DocumentSnapshot
  private var machine:Place?=null

  private val jobReference=db.collection("places").document(machineId)
    .collection("jobs").document(jobToRemove.id)
  private lateinit var jobDocument:DocumentSnapshot
  private var job:PrintJob?=null

  private val jobDestReference=db.collection("places").document("pool")
    .collection("jobs").document()

  override fun apply(transaction: Transaction) {

    //Check pool
    poolDocument=transaction.get(poolReference)
    if(poolDocument.exists()){
      pool=poolDocument.toObject(Place::class.java)
      pool?.let {
        it.jobCount++
        it.jobCounter++
      }
    }else{
      throw FirebaseFirestoreException(
        "Pool not found",
        FirebaseFirestoreException.Code.ABORTED)
    }

    //Check Job
    jobDocument=transaction.get(jobReference)
    if(jobDocument.exists()){
      job=jobDocument.toObject(PrintJob::class.java)
      job?.position=pool!!.jobCounter.toDouble()
      job?.id=jobDestReference.id
    }else
      throw FirebaseFirestoreException(
        "Job not found",
        FirebaseFirestoreException.Code.ABORTED)


    //check machine
    machineDocument=transaction.get(machineReference)
    if(machineDocument.exists()){
      machine=machineDocument.toObject(Place::class.java)
      machine?.let {
        it.jobCount--
        it.duration-=job!!.runningTime
      }
    }else
      throw FirebaseFirestoreException(
        "Machine not found",
        FirebaseFirestoreException.Code.ABORTED)


    pool?.let {
      it.duration+=job!!.runningTime
      transaction.set(poolReference,it)
    }
    transaction.set(machineReference,machine!!)
    transaction.set(jobDestReference,job!!)
    transaction.delete(jobReference)
  }
}
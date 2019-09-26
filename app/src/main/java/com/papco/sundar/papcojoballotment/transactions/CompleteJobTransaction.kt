package com.papco.sundar.papcojoballotment.transactions

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Transaction
import com.papco.sundar.papcojoballotment.documents.Place
import com.papco.sundar.papcojoballotment.documents.PrintJob
import java.util.*

class CompleteJobTransaction(jobToRemove: PrintJob, machineId:String):Transaction.Function<Unit> {

  val db=FirebaseFirestore.getInstance()

  private val completedReference=db.collection("places").document("completed")
  private lateinit var completedDocument:DocumentSnapshot
  private var completed:Place?=null

  private val machineReference=db.collection("places").document(machineId)
  private lateinit var machineDocument:DocumentSnapshot
  var machine:Place?=null

  private val jobReference=db.collection("places").document(machineId)
    .collection("jobs").document(jobToRemove.id)
  private lateinit var jobDocument:DocumentSnapshot
  var job:PrintJob?=null

  private val jobDestReference=db.collection("places").document("completed")
    .collection("jobs").document()

  override fun apply(transaction:Transaction) {

    //Check Job
    jobDocument=transaction.get(jobReference)
    if(jobDocument.exists()){
      job=jobDocument.toObject(PrintJob::class.java)
      job?.completedOn=Calendar.getInstance().timeInMillis
    }else
      throw FirebaseFirestoreException(
        "Job not found",
        FirebaseFirestoreException.Code.ABORTED)

    //Check Completed
    completedDocument=transaction.get(completedReference)
    if(completedDocument.exists()){
      completed=completedDocument.toObject(Place::class.java)
      completed?.let { it.jobCount++ }
    }else{
      completed= Place()
      completed?.let {
        it.id="completed"
        it.name="Completed jobs"
        it.jobCount=1
        it.reserved=true
      }
    }

    //check machine
    //Check Job
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


    transaction.set(completedReference,completed!!)
    transaction.set(machineReference,machine!!)
    transaction.set(jobDestReference,job!!)
    transaction.delete(jobReference)
  }
}
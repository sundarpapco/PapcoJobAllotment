package com.papco.sundar.papcojoballotment.transactions

import com.google.firebase.firestore.*
import com.papco.sundar.papcojoballotment.documents.Place
import com.papco.sundar.papcojoballotment.documents.PrintJob
import com.papco.sundar.papcojoballotment.utility.Duration

class AllotJobsTransaction(private val jobIds:MutableSet<String>,val placeId:String)
  :Transaction.Function<Unit>{

  private val db=FirebaseFirestore.getInstance()

  private lateinit var jobReference:DocumentReference
  private lateinit var jobDocument:DocumentSnapshot
  private var job:PrintJob?=null

  private val placeReference=db.collection("places").document(placeId)
  private lateinit var placeDocument:DocumentSnapshot
  private var place:Place?=null

  private val poolReference=db.collection("places").document("pool")
  private lateinit var poolDocument:DocumentSnapshot
  private var pool:Place?=null

  private val jobsToMove:MutableList<PrintJob> =ArrayList()
  private var totalTimeOfMovingJobs= Duration()

  override fun apply(transaction: Transaction) {

    //Destination Place document
    placeDocument=transaction.get(placeReference)
    if(placeDocument.exists()){
      place=placeDocument.toObject(Place::class.java)
    }else
      throw FirebaseFirestoreException(
        "Destination machine not found",
        FirebaseFirestoreException.Code.ABORTED)

    //Destination Place document
    poolDocument=transaction.get(poolReference)
    if(poolDocument.exists()){
      pool=poolDocument.toObject(Place::class.java)
    }else
      throw FirebaseFirestoreException(
        "Pool document not found",
        FirebaseFirestoreException.Code.ABORTED)

    //Read all the Jobs
    var positionIndex=0
    place?.let { positionIndex=it.jobCounter+1 }

    for(id in jobIds){
      jobReference=db.collection("places").document("pool")
        .collection("jobs").document(id)
      jobDocument=transaction.get(jobReference)
      if(jobDocument.exists()){
        job=jobDocument.toObject(PrintJob::class.java)
        job?.let{
          it.position=positionIndex.toDouble()
          positionIndex++
          jobsToMove.add(it)
          totalTimeOfMovingJobs+=it.runningTime
        }
      }else
        throw FirebaseFirestoreException(
          "Some jobs not found while trying to allot",
          FirebaseFirestoreException.Code.ABORTED)

    }

    for(id in jobIds){
      jobReference=db.collection("places").document("pool")
        .collection("jobs").document(id)
      transaction.delete(jobReference)
    }

    for(printJob in jobsToMove){
      jobReference=db.collection("places").document(placeId)
        .collection("jobs").document()
      printJob.id=jobReference.id
      transaction.set(jobReference,printJob)
    }

    place?.let {
      it.duration+=totalTimeOfMovingJobs
      it.jobCount+=jobsToMove.size
      it.jobCounter+=jobsToMove.size
      transaction.set(placeReference,it)
    }

    pool?.let {
      it.duration-=totalTimeOfMovingJobs
      it.jobCount-=jobsToMove.size
      transaction.set(poolReference,it)
    }


  }
}
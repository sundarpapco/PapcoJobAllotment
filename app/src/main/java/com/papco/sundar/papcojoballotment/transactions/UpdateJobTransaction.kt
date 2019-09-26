package com.papco.sundar.papcojoballotment.transactions

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Transaction
import com.papco.sundar.papcojoballotment.documents.Place
import com.papco.sundar.papcojoballotment.documents.PrintJob

class UpdateJobTransaction(private val job:PrintJob): Transaction.Function<Unit> {

  private val db=FirebaseFirestore.getInstance()
  private val poolDocumentRef=db.collection("places").document("pool")
  private val jobDocumentRef=db.collection("places").document("pool")
    .collection("jobs").document(job.id)


  override fun apply(transaction: Transaction) {

    val poolObject:Place?
    val oldJob:PrintJob?

    val jobDocument=transaction.get(jobDocumentRef)
    if(jobDocument.exists())
      oldJob=jobDocument.toObject(PrintJob::class.java)
    else
      throw FirebaseFirestoreException(
        "Error:Job not found",
        FirebaseFirestoreException.Code.ABORTED
      )

    val poolDocument=transaction.get(poolDocumentRef)
    if(poolDocument.exists()) {
      poolObject = poolDocument.toObject(Place::class.java)
      poolObject?.let{
        if(oldJob!=null)
          it.duration-=oldJob.runningTime
        it.duration+=job.runningTime
      }

    }else
      throw FirebaseFirestoreException(
        "Error:Job not found",
        FirebaseFirestoreException.Code.ABORTED
      )

    poolObject?.let { transaction.set(poolDocumentRef,poolObject)}
    transaction.set(jobDocumentRef,job)

  }

}
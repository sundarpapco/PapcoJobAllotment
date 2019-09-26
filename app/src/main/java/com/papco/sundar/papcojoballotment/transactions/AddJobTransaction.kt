package com.papco.sundar.papcojoballotment.transactions

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Transaction
import com.papco.sundar.papcojoballotment.documents.Place
import com.papco.sundar.papcojoballotment.documents.PrintJob

class AddJobTransaction(private val job:PrintJob): Transaction.Function<Unit> {

    private val db=FirebaseFirestore.getInstance()
    private val poolDocumentRef=db.collection("places").document("pool")
    private val jobDocumentRef=db.collection("places").document("pool")
        .collection("jobs").document()

    override fun apply(transaction: Transaction) {

        val poolObject:Place?
        val poolDocument=transaction.get(poolDocumentRef)

        if(poolDocument.exists()) {
            poolObject = poolDocument.toObject(Place::class.java)
            poolObject?.let{
                it.duration=it.duration+job.runningTime
                it.jobCount++
                it.jobCounter++
                job.position=it.jobCounter.toDouble()
            }

        }else{
            poolObject=Place()
            poolObject.id="pool"
            poolObject.name="pool"
            poolObject.reserved=true
            poolObject.duration=job.runningTime
            poolObject.jobCount=1
            poolObject.jobCounter=1
            job.position=1.0
        }

        poolObject?.let { transaction.set(poolDocumentRef,poolObject)}
        transaction.set(jobDocumentRef,job)

    }

}
package com.papco.sundar.papcojoballotment.transactions

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Transaction

class DeletePlaceTransaction(placeId:String):Transaction.Function<Unit> {

    val db=FirebaseFirestore.getInstance()
    private val docReference=db.collection("places").document(placeId)

    override fun apply(transaction: Transaction) {

        val docSnapshot=transaction.get(docReference)
        if(docSnapshot.exists()){
            docSnapshot.getLong("jobCount")?.let {
                if(it==0.toLong())
                    transaction.delete(docReference)
                else
                    throw FirebaseFirestoreException(
                        "Cannot delete when there is jobs allotted to this machine",
                        FirebaseFirestoreException.Code.ABORTED)
            }
        }

    }

}
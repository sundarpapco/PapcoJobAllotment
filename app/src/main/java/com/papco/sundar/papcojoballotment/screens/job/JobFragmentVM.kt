package com.papco.sundar.papcojoballotment.screens.job

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Transaction
import com.papco.sundar.papcojoballotment.common.toast
import com.papco.sundar.papcojoballotment.documents.PrintJob
import com.papco.sundar.papcojoballotment.transactions.AddJobTransaction
import com.papco.sundar.papcojoballotment.transactions.UpdateJobTransaction
import com.papco.sundar.papcojoballotment.utility.EventMessage

class JobFragmentVM(app:Application):AndroidViewModel(app) {

    private val db=FirebaseFirestore.getInstance()
    val eventBus=MutableLiveData<EventMessage>()
    private var loadedJob:PrintJob= PrintJob()
    private var alreadyLoaded=false
    private var documentWatcher:ListenerRegistration?=null

    fun loadJob(jobId:String){
        documentWatcher=db.collection("places").document("pool")
            .collection("jobs").document(jobId)
            .addSnapshotListener{documentSnapshot,_ ->
                documentSnapshot?.let {
                    if(it.exists())
                        postJobIfNecessary(it)
                    else
                        eventBus.postValue(EventMessage(EventMessage.EVENT_JOB_DELETED,""))
                }
            }

    }

    fun addNewJob(newJob:PrintJob){
        runTransaction(AddJobTransaction(newJob))
    }

    fun updateJob(updatedJob:PrintJob){
        updatedJob.id=loadedJob.id
        updatedJob.position=loadedJob.position
        runTransaction(UpdateJobTransaction(updatedJob))
    }

    private fun runTransaction(transaction:Transaction.Function<Unit>){
        eventBus.postValue(EventMessage(EventMessage.EVENT_TRANSACTION_START,"One moment please..."))

        db.runTransaction(transaction)
            .addOnSuccessListener {
                eventBus.postValue(EventMessage(EventMessage.EVENT_TRANSACTION_END,"success",true))
            }
            .addOnFailureListener{
                toast("${it.message}\nPlease check internet connection")
                eventBus.postValue(EventMessage(EventMessage.EVENT_TRANSACTION_END,"Job add failed",false))
            }
    }

    private fun postJobIfNecessary(documentSnapshot: DocumentSnapshot){
        documentSnapshot.toObject(PrintJob::class.java)?.let {
            loadedJob=it
            loadedJob.id=documentSnapshot.id
            if(!alreadyLoaded) {
                eventBus.postValue(EventMessage(EventMessage.EVENT_LOAD_JOB, loadedJob))
                alreadyLoaded=true
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        documentWatcher?.remove()
    }
}
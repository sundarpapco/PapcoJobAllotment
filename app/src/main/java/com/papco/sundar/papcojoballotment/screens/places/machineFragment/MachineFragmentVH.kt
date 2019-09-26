package com.papco.sundar.papcojoballotment.screens.places.machineFragment

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.*
import com.papco.sundar.papcojoballotment.common.toast
import com.papco.sundar.papcojoballotment.documents.Place
import com.papco.sundar.papcojoballotment.documents.PrintJob
import com.papco.sundar.papcojoballotment.transactions.CompleteJobTransaction
import com.papco.sundar.papcojoballotment.transactions.RemoveFromQueueTransaction
import com.papco.sundar.papcojoballotment.utility.EventMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class MachineFragmentVH(app:Application):AndroidViewModel(app) {

  val db=FirebaseFirestore.getInstance()

  val jobsList=MutableLiveData<MutableList<PrintJob>>()
  val machineDocument=MutableLiveData<Place?>()
  val eventBus=MutableLiveData<EventMessage>()

  private var jobsListener:ListenerRegistration?=null
  private var machineListener:ListenerRegistration?=null
  private var placeId=""

  //selectedJob will hold the job value which is selected for completion or removal
  //selectedJob is stored here temporarily just before showing the confirmation dialog
  // so that after the confirmation dialog, we will have the job instance to act upon.
  var selectedJob=PrintJob()

  fun loadJobs(placeId:String){

    this.placeId=placeId
    machineListener=db.collection("places").document(placeId)
      .addSnapshotListener{documentSnapshot, _ ->
        documentSnapshot?.let{processAndPublishDocument(it)}
      }

    jobsListener=db.collection("places").document(placeId)
      .collection("jobs").orderBy("position")
      .addSnapshotListener{querySnapshot, _ ->
        processAndPublishList(querySnapshot)
      }
  }

  fun moveJob(movedJob:PrintJob){
    val newValues= HashMap<String,Any>()
    newValues["position"]=movedJob.position
    db.collection("places").document(placeId)
      .collection("jobs").document(movedJob.id)
      .update(newValues)
      .addOnFailureListener{
        toast("Failed to update: ${it.message}")
      }
  }

  fun completeJob(){
    eventBus.postValue(EventMessage(EventMessage.EVENT_TRANSACTION_START,"One moment please"))
    runTransaction(CompleteJobTransaction(selectedJob,placeId))

  }

  fun removeJobFromQueue(){
    eventBus.postValue(EventMessage(EventMessage.EVENT_TRANSACTION_START,"One moment please"))
    runTransaction(RemoveFromQueueTransaction(selectedJob,placeId))
  }

  private fun runTransaction(transaction: Transaction.Function<Unit>){

    db.runTransaction(transaction)
      .addOnSuccessListener {
        eventBus.postValue(EventMessage(EventMessage.EVENT_TRANSACTION_END,"success",true))
      }
      .addOnFailureListener{
        var result="failed"
        it.message?.let {msg-> result=msg }
        eventBus.postValue(EventMessage(EventMessage.EVENT_TRANSACTION_END,result,false))
      }

  }

  private fun processAndPublishDocument(documentSnapshot: DocumentSnapshot){
    if(!documentSnapshot.exists())
      machineDocument.postValue(null)
    else{
      documentSnapshot.toObject(Place::class.java)?.let {
        machineDocument.postValue(it)
      }
    }
  }

  private fun processAndPublishList(querySnapshot: QuerySnapshot?) {

    viewModelScope.launch(Dispatchers.Default){
      querySnapshot?.documents?.let {documents->
        jobsList.postValue(convertList(documents))
      }
    }

  }

  private fun convertList(documents:List<DocumentSnapshot>):MutableList<PrintJob>{

    val result:MutableList<PrintJob> = LinkedList()
    for(document in documents){
      document.toObject(PrintJob::class.java)?.let {
        it.id=document.id
        it.calculateAge()
        result.add(it)
      }
    }

    return result
  }

  override fun onCleared() {
    super.onCleared()
    jobsListener?.remove()
    machineListener?.remove()
  }
}
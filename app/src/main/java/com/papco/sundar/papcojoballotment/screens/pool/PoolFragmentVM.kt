package com.papco.sundar.papcojoballotment.screens.pool

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.Transaction
import com.papco.sundar.papcojoballotment.common.toast
import com.papco.sundar.papcojoballotment.documents.Place
import com.papco.sundar.papcojoballotment.documents.PrintJob
import com.papco.sundar.papcojoballotment.transactions.AllotJobsTransaction
import com.papco.sundar.papcojoballotment.transactions.DeleteJobsTransaction
import com.papco.sundar.papcojoballotment.utility.EventMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class PoolFragmentVM(app: Application) : AndroidViewModel(app) {

  private val db = FirebaseFirestore.getInstance()
  val poolDocumentWatcher = MutableLiveData<Place>()
  val jobsList=MutableLiveData<MutableList<PrintJob>>()
  var jobsSelection=JobsSelection()
  val eventBus=MutableLiveData<EventMessage>()

  private val poolDocumentListener = db.collection("places").document("pool")
    .addSnapshotListener { documentSnapshot, _ ->
      documentSnapshot?.let {
        if (!it.exists()) {
          val pool=Place()
          pool.id="pool"
          pool.name="pool"
          pool.reserved=true
          poolDocumentWatcher.postValue(pool)
        }else
          poolDocumentWatcher.postValue(it.toObject(Place::class.java))
      }

    }

  private val jobsListener=db.collection("places").document("pool")
    .collection("jobs").orderBy("position")
    .addSnapshotListener{querySnapshot, _ ->
      processAndPublishList(querySnapshot)
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

  fun deleteJobs(jobIds:MutableSet<String>){
    eventBus.postValue(EventMessage(EventMessage.EVENT_TRANSACTION_START,"One moment please"))
    runTransaction(DeleteJobsTransaction(jobIds))
  }

  fun allotJobs(placeId:String,jobIds:MutableSet<String>){
    eventBus.postValue(EventMessage(EventMessage.EVENT_TRANSACTION_START,"One moment please"))
    runTransaction(AllotJobsTransaction(jobIds,placeId))
  }

  fun moveJob(movedJob:PrintJob){
    val newValues= HashMap<String,Any>()
    newValues["position"]=movedJob.position
    db.collection("places").document("pool")
      .collection("jobs").document(movedJob.id)
      .update(newValues)
      .addOnFailureListener{
        toast("Failed to update: ${it.message}")
      }
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

  override fun onCleared() {
    super.onCleared()
    poolDocumentListener.remove()
    jobsListener.remove()
  }
}
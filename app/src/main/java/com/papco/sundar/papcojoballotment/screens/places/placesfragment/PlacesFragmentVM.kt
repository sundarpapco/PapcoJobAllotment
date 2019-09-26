package com.papco.sundar.papcojoballotment.screens.places.placesfragment

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.papco.sundar.papcojoballotment.common.toast
import com.papco.sundar.papcojoballotment.documents.Place
import com.papco.sundar.papcojoballotment.transactions.DeletePlaceTransaction
import com.papco.sundar.papcojoballotment.utility.EventMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PlacesFragmentVM(app: Application) : AndroidViewModel(app) {

    val placesList = MutableLiveData<List<Place>>()
    val eventBus = MutableLiveData<EventMessage>()
    private val db = FirebaseFirestore.getInstance()
    private val listenerRegistration = db.collection("places")
        .whereEqualTo("reserved",false).orderBy("name")
        .addSnapshotListener { querySnapshot, exception ->

            exception?.let {
                toast("Error fetching data from server")
                return@addSnapshotListener
            }

            querySnapshot?.let { processAndPublishList(querySnapshot) }

        }

    private fun processAndPublishList(querySnapshot: QuerySnapshot) {

        viewModelScope.launch(Dispatchers.Default) {
            placesList.postValue(convertList(querySnapshot.documents))
        }

    }

    private fun convertList(documents: List<DocumentSnapshot>?): List<Place> {

        if (documents == null)
            return ArrayList()

        val resultList: MutableList<Place> = ArrayList()


        for (i in documents) {

            i.toObject(Place::class.java)?.let {
                it.id = i.id
                resultList.add(it)
            }
        }

        return resultList
    }

    fun addPlace(placeName:String){
        val place=Place()
        place.name=placeName
        db.collection("places").document().set(place)
            .addOnSuccessListener {
                toast("Machine added successfully")
            }
            .addOnFailureListener{
                toast("Failed adding machine: ${it.message}")
            }
    }

    fun updatePlace(placeId:String,placeName:String){
        val newValues= HashMap<String,Any>()
        newValues["id"]=placeId
        newValues["name"]=placeName
        db.collection("places").document(placeId).update(newValues)
            .addOnSuccessListener {
                toast("Updated successfully")
            }
            .addOnFailureListener{
                toast("Failed to update: ${it.message}")
            }
    }

    fun deletePlace(placeId:String){

        eventBus.postValue(EventMessage(EventMessage.EVENT_TRANSACTION_START,"Deleting. Please wait..."))

        db.runTransaction(DeletePlaceTransaction(placeId))
            .addOnSuccessListener {
                val msg="Machine deleted successfully"
                toast(msg)
                eventBus.postValue(EventMessage(EventMessage.EVENT_TRANSACTION_END,msg))
            }
            .addOnFailureListener{
                it.message?.let {message->
                    val msg="Delete failed\n$message\nPlease check internet connection also"
                    toast(msg)
                    eventBus.postValue(EventMessage(EventMessage.EVENT_TRANSACTION_END,msg))
                }
            }
    }

    override fun onCleared() {
        super.onCleared()
        listenerRegistration.remove()
    }
}
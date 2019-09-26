package com.papco.sundar.papcojoballotment.screens.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.papco.sundar.papcojoballotment.documents.Place
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeFragmentVM(app: Application) : AndroidViewModel(app) {

    private val db = FirebaseFirestore.getInstance()
    val poolDocumentLive = MutableLiveData<Place>()
    val machinesDocumentLive = MutableLiveData<Place>()
    val totalDocumentLive = MutableLiveData<Place>()
    private var poolDocument = Place()
    private var machinesDocument = Place()
    private val poolListener = db.collection("places").document("pool")
        .addSnapshotListener { documentSnapshot, _ ->
            documentSnapshot?.let {
                postPoolDocument(documentSnapshot)
            }
        }

    private val machinesListener = db.collection("places").whereEqualTo("reserved", false)
        .addSnapshotListener { querySnapshot, _ ->
            querySnapshot?.let {
                processListAndUpdateMachineDocument(querySnapshot)
            }
        }

    private fun postPoolDocument(documentSnapshot: DocumentSnapshot) {
        val pool: Place?
        if (documentSnapshot.exists()) {
            pool = documentSnapshot.toObject(Place::class.java)
            poolDocumentLive.postValue(pool)
            updateTotalDocument()
        } else {
            pool = Place()
            pool.id = "pool"
            pool.reserved = true
            pool.name = "pool"
            poolDocumentLive.value = pool
        }

        poolDocument = pool!!
        updateTotalDocument()

    }


    private fun updateTotalDocument() {
        val result = Place()

        result.jobCount += poolDocument.jobCount
        result.duration += poolDocument.duration

        result.jobCount += machinesDocument.jobCount
        result.duration += machinesDocument.duration

        totalDocumentLive.value = result
    }

    private fun processListAndUpdateMachineDocument(querySnapShot: QuerySnapshot) {

        viewModelScope.launch(Dispatchers.Default) {
            val documents = querySnapShot.documents
            val result = Place()
            result.id = "machines"
            result.name = "machines"
            for (doc in documents) {
                doc.toObject(Place::class.java)?.let {
                    result.jobCount += it.jobCount
                    result.duration += it.duration
                }
            }
            withContext(Dispatchers.Main) {
                machinesDocument = result
                machinesDocumentLive.value = result
                updateTotalDocument()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        poolListener.remove()
        machinesListener.remove()
    }
}
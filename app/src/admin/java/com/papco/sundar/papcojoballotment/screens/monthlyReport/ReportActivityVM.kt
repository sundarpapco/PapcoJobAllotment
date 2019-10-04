package com.papco.sundar.papcojoballotment.screens.monthlyReport

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.papco.sundar.papcojoballotment.chart.ChartValues
import kotlinx.coroutines.*

class ReportActivityVM(app: Application) : AndroidViewModel(app) {

    val db = FirebaseFirestore.getInstance()
    val chartDetail: MutableLiveData<ChartValues> = MutableLiveData()
    var chartLoadingJob:Job?=null
    var loadedMonth=-1
    var loadedYear=-1

    fun loadValues(monthOfYear: Int, year: Int) {

        chartLoadingJob?.cancel()
        loadedMonth=monthOfYear
        loadedYear=year
        val chartValues = ChartValues(monthOfYear, year)
        db.collection("places").document("completed").collection("jobs")
            .whereGreaterThanOrEqualTo("completedOn", chartValues.startingTime)
            .whereLessThanOrEqualTo("completedOn", chartValues.endingTime)
            .orderBy("completedOn")
            .get()
            .addOnSuccessListener {
                prepareChartValuesInBackground(chartValues, it.documents)
            }
    }

    private fun prepareChartValuesInBackground(
        chartValues: ChartValues,
        documents: List<DocumentSnapshot?>
    ) {
        chartLoadingJob=viewModelScope.launch(Dispatchers.Default) {
            chartValues.loadChartPoints(documents)
            //if(isActive)
                chartDetail.postValue(chartValues)
        }
    }
}
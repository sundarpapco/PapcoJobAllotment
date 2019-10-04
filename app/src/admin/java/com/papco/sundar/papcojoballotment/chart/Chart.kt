package com.papco.sundar.papcojoballotment.chart

import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.google.firebase.firestore.DocumentSnapshot
import com.papco.sundar.papcojoballotment.documents.PrintJob
import com.papco.sundar.papcojoballotment.utility.Duration
import java.util.*
import kotlin.collections.ArrayList

data class ChartPoint(var dayOfMonth:Int=1,var duration: Duration=Duration())

class ChartValues(val monthOfYear:Int,val year:Int){

    val startingTime by lazy{
        val date= Calendar.getInstance(Locale.getDefault())
        date.set(Calendar.YEAR,year)
        date.set(Calendar.MONTH,monthOfYear)
        date.set(Calendar.DAY_OF_MONTH,1)
        date.set(Calendar.HOUR_OF_DAY,0)
        date.set(Calendar.MINUTE,0)
        date.set(Calendar.SECOND,0)
        date.set(Calendar.MILLISECOND,0)
        date.timeInMillis
    }

    val endingTime by lazy {
        val date=Calendar.getInstance(Locale.getDefault())
        date.set(Calendar.YEAR,year)
        date.set(Calendar.DAY_OF_MONTH,1)
        date.set(Calendar.MONTH,monthOfYear)
        date.set(Calendar.DAY_OF_MONTH,date.getActualMaximum(Calendar.DAY_OF_MONTH))
        date.set(Calendar.HOUR_OF_DAY,23)
        date.set(Calendar.MINUTE,59)
        date.set(Calendar.SECOND,59)
        date.set(Calendar.MILLISECOND,999)
        date.timeInMillis
    }

    val daysInThisMonth:Int by lazy{
        val date=Calendar.getInstance(Locale.getDefault())
        date.set(Calendar.YEAR,year)
        date.set(Calendar.DAY_OF_MONTH,1)
        date.set(Calendar.MONTH,monthOfYear)
        date.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    var barData:BarData?=null
        private set

    var totalDuration=Duration()
        private set

    var noOfWorkingDays=0
        private set

    var isChartPointsLoaded=false
        private set

    fun loadChartPoints(documents:List<DocumentSnapshot?>){

        if(isChartPointsLoaded)
            return

        var chartPoints:MutableList<BarEntry> =ArrayList()
        var jobListIndex=0
        var chartPoint: ChartPoint
        var printJob:PrintJob?
        for(currentDate in 1..daysInThisMonth){
            chartPoint= ChartPoint(currentDate)
            while(jobListIndex<documents.size){
                printJob=documents[jobListIndex]?.toObject(PrintJob::class.java)
                if(printJob==null){
                    jobListIndex++
                    continue
                }
                if(day(printJob.completedOn)!=currentDate){
                    break
                }
                chartPoint.duration+=printJob.runningTime
                jobListIndex++
            }
            if(chartPoint.duration.asDecimal()>0.0) {
                totalDuration+=chartPoint.duration
                noOfWorkingDays++
            }
            chartPoints.add(BarEntry(chartPoint.dayOfMonth.toFloat(),chartPoint.duration.asDecimal().toFloat()))
        }

        prepareBarData(chartPoints)

        isChartPointsLoaded=true
    }

    private fun day(time:Long):Int{
        val calendar=Calendar.getInstance(Locale.getDefault())
        calendar.timeInMillis=time
        return calendar.get(Calendar.DAY_OF_MONTH)
    }

    private fun prepareBarData(chartPoints:MutableList<BarEntry>){

        val barDataSet = BarDataSet(chartPoints,"Production time in Hours")
        barDataSet.setDrawIcons(false)

        val dataSets:MutableList<IBarDataSet> =ArrayList()
        dataSets.add(barDataSet)

        barData = BarData(dataSets)
        barData?.setValueTextSize(10f)
        barData?.barWidth = 0.9f
        barData?.setValueFormatter(BarValueFormatter())

    }

}
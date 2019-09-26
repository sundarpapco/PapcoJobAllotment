package com.papco.sundar.papcojoballotment.documents

import com.google.firebase.firestore.Exclude
import com.papco.sundar.papcojoballotment.common.DatePickerFragment
import com.papco.sundar.papcojoballotment.utility.Duration
import java.text.SimpleDateFormat
import java.util.*


data class PrintJob(
    var id: String = "_id",
    var date: String="17/09/2019",
    var poNumber: String = "0",
    var client: String = "Dafault customer",
    var paper: String = "Some paper",
    var color: String = "CMYK",
    var runningTime: Duration = Duration(1, 30),
    var position: Double = 0.0,
    var completedOn: Long = 0,
    var pendingReason:String=""
) {
    @Exclude private val oneDay: Long = 86400000
    @Exclude var ageString=""

    fun calculateAge() {

        val dateFormat=SimpleDateFormat(DatePickerFragment.DATE_FORMAT,Locale.getDefault())
        val dateCreated=dateFormat.parse(date)
        val daysDifference = (Calendar.getInstance(Locale.getDefault()).timeInMillis-dateCreated.time)/oneDay

        ageString=when (daysDifference) {

            0L -> {
                "Today"
            }
            1L->{
                "Yesterday"
            }
            else -> {
                "$daysDifference Days ago"
            }
        }
    }
}
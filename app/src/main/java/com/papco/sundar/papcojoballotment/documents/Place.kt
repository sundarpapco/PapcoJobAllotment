package com.papco.sundar.papcojoballotment.documents

import com.papco.sundar.papcojoballotment.utility.Duration

data class Place(
  var id:String="_id",
  var name:String="Machine",
  var duration:Duration = Duration(0,0),
  var jobCount:Int=0,
  var jobCounter:Int=0,
  var reserved:Boolean=false){

  fun jobDetails():String{
    return "${duration.asString()} in $jobCount jobs"
  }

}
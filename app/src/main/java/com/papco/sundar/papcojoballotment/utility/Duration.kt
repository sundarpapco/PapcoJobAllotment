package com.papco.sundar.papcojoballotment.utility

import kotlin.math.roundToInt

class Duration(hours: Int = 0, minutes: Int = 0) {

    companion object {

        fun fromMinutes(minutes: Int): Duration {
            return Duration(minutes / 60, minutes % 60)
        }
    }


    var hours:Int=0
    set(value){
        field = if(value<0) 0 else value
    }

    var minutes:Int=0
    set(value){
        when{
            value < 0 ->{ field=0}
            value >=60 ->{
                hours+=value/60
                field = value%60
            }
            else->{field=value}
        }
    }

    init {
        this.hours=hours
        this.minutes= minutes
    }

    operator fun plus(duration: Duration): Duration {
        return fromMinutes(inMinutes() + duration.inMinutes())
    }

    operator fun minus(duration: Duration): Duration {

        val resultInMinutes = inMinutes() - duration.inMinutes()
        return when {
            resultInMinutes < 0 -> {
                Duration(0, 0)
            }

            else -> {
                fromMinutes(resultInMinutes)
            }
        }
    }

    fun divideBy(number:Int):Duration{
        if(number==0)
            return this

        val resultMins=inMinutes()/number
        return Duration(0,resultMins)
    }

    private fun inMinutes(): Int {
        return hours * 60 + minutes
    }


    fun asString(): String {
        return "$hours hrs, $minutes mins"
    }

    override fun toString(): String {
        return asString()
    }

    fun asDetailString(): String {
        return "$hours Hours, $minutes Mins"
    }

    fun daysOfWork(): String {
        var result = 0.0
        val workingMinsPerDay = 11*2* 60 //11 hours X 2 Machines X 60 minutes per hour
        val days = inMinutes() / workingMinsPerDay
        val noOfDays: String
        val dayOrDays: String

        when(inMinutes() % workingMinsPerDay) {

            0 -> {
                result = days.toDouble()
            }
            in 1..(workingMinsPerDay/2) -> {
                result = days + 0.5
            }
            in workingMinsPerDay/2+1..workingMinsPerDay -> {
                result = days+1.0
            }
        }
        dayOrDays = if (result <= 1.0) {
            "day"
        } else
            "days"

        noOfDays = if (result - result.toInt() == 0.0)
            result.toInt().toString()
        else
            result.toString()

        return "$noOfDays $dayOrDays of work approx"
    }

    fun asDecimal():Double{
        return hours.toDouble()+ ((minutes.toDouble()/60.0*100).roundToInt()).toDouble()/100.0
    }

    override fun equals(other: Any?): Boolean {
        if(other==null) return false
        val arg:Duration
        try{
            arg=other as Duration
        }catch (e:Exception){
            return false
        }
        return hours==arg.hours && minutes==arg.minutes
    }
}
package com.papco.sundar.papcojoballotment.utility

data class Duration(var hours: Int = 0, var minutes: Int = 0) {

    companion object {

        fun fromMinutes(minutes: Int): Duration {
            return Duration(minutes / 60, minutes % 60)
        }
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

    fun daysOfWork(workingHourPerDay: Int): String {
        var result = 0.0
        val workingMinsPerDay = workingHourPerDay * 60
        val days = inMinutes() / (workingMinsPerDay*2)
        val noOfDays: String
        val dayOrDays: String

        when(inMinutes() % workingMinsPerDay) {

            0 -> {
                result = days.toDouble()
            }
            in 1..(workingMinsPerDay/2) -> {
                result = days + 0.5
            }
            in 331..workingMinsPerDay -> {
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
}
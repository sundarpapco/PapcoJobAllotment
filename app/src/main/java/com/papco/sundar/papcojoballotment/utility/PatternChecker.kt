package com.papco.sundar.papcojoballotment.utility

import java.util.regex.Pattern

class PatternChecker(private val userInput: String) {

    private val regex =
        "[1-9][0-9]*(.[1-9])?[SsGgRr]?([Xx][0-9]+[FfHh]?[Ee]?)?(\\+[1-9][0-9]*(.[1-9])?[SsGgRr]?([Xx][0-9]+[FfHh]?[Ee]?)?)*"

    private var jobSets: List<String> = userInput.split("+")

    val isValid: Boolean by lazy {
        Pattern.matches(regex, userInput)
    }

    val hasExtraColour:Boolean by lazy{
        userInput.indexOf("e",0,true)!=-1
    }

    private fun sheetsPerSet(inputString: String): Int {

        if (!isValid) return -1

        val xIndex = inputString.indexOf("x", 0, true)
        val sIndex = inputString.indexOf("s", 0, true)
        val gIndex = inputString.indexOf("g", 0, true)
        val rIndex = inputString.indexOf("r", 0, true)
        val isThisHalfForm = isSetHalfForm(inputString)
        return when {
            sIndex != -1 -> {
                if (isThisHalfForm)
                    (inputString.substring(0, sIndex).toDouble() * 2).toInt()
                else
                    inputString.substring(0, sIndex).toDouble().toInt()
            }
            gIndex != -1 -> {
                if (isThisHalfForm)
                    (inputString.substring(0, gIndex).toDouble() * 144 * 2).toInt()
                else
                    (inputString.substring(0, gIndex).toDouble() * 144).toInt()
            }
            rIndex != -1 -> {
                if (isThisHalfForm)
                    (inputString.substring(0, rIndex).toDouble() * 500 * 2).toInt()
                else
                    (inputString.substring(0, rIndex).toDouble() * 500).toInt()
            }
            xIndex != -1 -> {
                if (isThisHalfForm)
                    (inputString.substring(0, xIndex).toDouble() * 2).toInt()
                else
                    inputString.substring(0, xIndex).toDouble().toInt()
            }
            else -> {
                if (isThisHalfForm)
                    (inputString.toDouble() * 2).toInt()
                else
                    inputString.toDouble().toInt()
            }
        }
    }

    private fun sets(inputString: String): Int {

        if (!isValid) return -1

        val xIndex = inputString.indexOf("x", 0, true)
        val fIndex = inputString.indexOf("f", 0, true)
        val hIndex = inputString.indexOf("h", 0, true)

        return when {
            xIndex == -1 -> {
                1
            }
            fIndex != -1 -> {
                inputString.substring(xIndex + 1, fIndex).toInt() * 2
            }
            hIndex != -1 -> {
                inputString.substring(xIndex + 1, hIndex).toInt()
            }
            else -> {
                if (isSetExtraColor(inputString))
                    inputString.substring(xIndex + 1, inputString.length - 1).toInt()
                else
                    inputString.substring(xIndex + 1, inputString.length).toInt()
            }
        }


    }

    private fun isSetExtraColor(inputString: String): Boolean {
        return inputString.indexOf("e", 0, true) != -1
    }

    private fun isSetHalfForm(inputString: String): Boolean {
        return inputString.indexOf("h", 0, true) != -1
    }

    private fun setRunningTime(inputString: String): Duration {
        var minutes =
            (0.012 * sheetsPerSet(inputString).toDouble() * sets(inputString).toDouble()).toInt()

        //If its same plate front and back, add 10 minutes as a drying time before turning
        if(isSetHalfForm(inputString))
            minutes+=10

        return Duration(0, minutes)
    }

    private fun setMakeReadyTime(inputString: String): Duration {
        var minutes = sets(inputString) * 30
        if (isSetExtraColor(inputString))
            minutes += 30
        return Duration(0, minutes)
    }

    fun makeReadyTime(): Duration {
        var duration = Duration()

        for (set in jobSets) {
            duration += setMakeReadyTime(set)
        }
        return duration
    }

    fun runningTime(): Duration {
        var duration = Duration()

        for (set in jobSets)
            duration += setRunningTime(set)

        return duration
    }

    fun totalTime(): Duration {
        return makeReadyTime() + runningTime()
    }

}
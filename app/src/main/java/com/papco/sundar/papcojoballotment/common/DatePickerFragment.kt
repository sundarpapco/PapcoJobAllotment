package com.papco.sundar.papcojoballotment.common

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

class DatePickerFragment : DialogFragment(), DatePickerDialog.OnDateSetListener {


    companion object {

        const val DATE_FORMAT = "dd/MM/yyyy"
        const val KEY_STARTING_DATE = "DatePicker:StartingDate"
        const val TAG="PapcoJobAllotment:DatePickerFragment"

        fun startWithDate(dateString: String): DatePickerFragment {
            val args = Bundle()
            args.putString(KEY_STARTING_DATE, dateString)
            return DatePickerFragment().also { it.arguments = args }
        }


        fun now(): String {
            return SimpleDateFormat(DATE_FORMAT,Locale.getDefault())
                .format(Calendar.getInstance().time)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val calendar = getCalendar()
        return DatePickerDialog(
            requireContext(),this, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        val calendar = Calendar.getInstance(Locale.getDefault())
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month)
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
        val dateFormatter = SimpleDateFormat(DATE_FORMAT,Locale.getDefault())
        val dateInString = dateFormatter.format(calendar.time)
        deliverResult(dateInString)
        dismiss()
    }

    private fun getCalendar(): Calendar {

        val dateObject=SimpleDateFormat(DATE_FORMAT,Locale.getDefault()).parse(getDateString())
        return Calendar.getInstance().also { it.time=dateObject }
    }

    private fun getDateString():String{

        arguments?.getString(KEY_STARTING_DATE)?.let { return it }
        return now()
    }

    private fun deliverResult(resultDate:String){

        try{
            when{
                parentFragment!=null->{
                    (parentFragment as DatePickerDialogListener).onDateSet(resultDate)
                    return
                }
                activity!=null->{
                    (requireActivity() as DatePickerDialogListener).onDateSet(resultDate)
                    return
                }
            }
        }catch (exception:Exception){
            toast("Caller should implement DatePickerDialogListener interface")
        }
    }

    interface DatePickerDialogListener{
        fun onDateSet(date:String)
    }
}
package com.papco.sundar.papcojoballotment.common

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.whiteelephant.monthpicker.MonthPickerDialog
import java.util.*

class MonthPickerDialogFragment : DialogFragment(), MonthPickerDialog.OnDateSetListener{

    companion object {

        const val TAG="PapcoJobAllotment:MonthPickerFragment"
        private const val KEY_STARTUP_MONTH = "key_startup_month"
        private const val KEY_STARTUP_YEAR = "key_startup_year"

        fun getInstance(startingMonth: Int, startingYear: Int): MonthPickerDialogFragment {
            val args = Bundle()
            args.putInt(KEY_STARTUP_MONTH, startingMonth)
            args.putInt(KEY_STARTUP_YEAR, startingYear)
            return MonthPickerDialogFragment().also { it.arguments = args }
        }
    }

    private val now = Calendar.getInstance(Locale.getDefault())

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val builder =
            MonthPickerDialog.Builder(requireContext(), this, startingYear(), startingMonth())
        builder.setMinYear(2017)
        if (startingYear() == now.get(Calendar.YEAR))
            builder.setMaxMonth(now.get(Calendar.MONTH))
        return builder.build()
    }

    override fun onDateSet(selectedMonth: Int, selectedYear: Int) {
        tryNotifyingObserver(selectedMonth,selectedYear)
    }


    private fun startingMonth(): Int {
        val defaultMonth = now.get(Calendar.MONTH)
        arguments?.let { return it.getInt(KEY_STARTUP_MONTH, defaultMonth) }
        return defaultMonth
    }

    private fun startingYear(): Int {
        val defaultYear = now.get(Calendar.YEAR)
        arguments?.let { return it.getInt(KEY_STARTUP_YEAR, defaultYear) }
        return defaultYear
    }

    private fun tryNotifyingObserver(month:Int,year:Int){
        try{
            when{
                parentFragment!=null->{
                    (parentFragment as MonthSelectionListener).onMonthSelected(month,year)
                    return
                }
                activity!=null->{
                    (activity as MonthSelectionListener).onMonthSelected(month,year)
                    return
                }
            }
        }catch (exception: Exception){
            toast("The caller should implement MonthSelectionListener")
        }
    }

    interface MonthSelectionListener{
        fun onMonthSelected(selectedMonth:Int,selectedYear:Int)
    }
}
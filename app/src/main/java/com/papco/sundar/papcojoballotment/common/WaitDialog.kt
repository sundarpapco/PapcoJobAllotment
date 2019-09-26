package com.papco.sundar.papcojoballotment.common

import android.app.Dialog
import android.app.ProgressDialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment

class WaitDialog:DialogFragment() {

    companion object{

        const val KEY_MESSAGE="Key_for_message"
        const val DEFAULT_MESSAGE="Please wait..."
        const val TAG_FRAGMENT="papcoJob:waitDialog"

        fun getInstance(msg:String): WaitDialog {
            val args=Bundle()
            args.putString(KEY_MESSAGE,msg)
            return WaitDialog().also {it.arguments=args}
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val progressDialog=ProgressDialog(requireActivity())
        progressDialog.isIndeterminate=true
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        progressDialog.setMessage(getMessage())
        progressDialog.setCancelable(false)
        isCancelable=false
        return progressDialog
    }


    private fun getMessage():String{

        arguments?.let {
            return it.getString(
                KEY_MESSAGE,
                DEFAULT_MESSAGE
            )
        }

        return DEFAULT_MESSAGE
    }

}
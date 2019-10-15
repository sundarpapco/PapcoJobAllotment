package com.papco.sundar.papcojoballotment.screens.job

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.papco.sundar.papcojoballotment.R
import com.papco.sundar.papcojoballotment.common.toast
import com.papco.sundar.papcojoballotment.utility.PatternChecker
import kotlinx.android.synthetic.main.fragment_dialog_quantity_expression.view.*
import kotlin.math.log

class QuantityExpFragment:DialogFragment() {

    companion object{
        const val TAG="PapcoJobAllotment:QuantityExpDialog"
    }

    private lateinit var enteredText: EditText
    private lateinit var buttonOk: TextView

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val inflater=requireActivity().layoutInflater
        val view=inflater.inflate(R.layout.fragment_dialog_quantity_expression,null,false)

        enteredText=view.fragment_dialog_quantity_expression_expression
        buttonOk=view.fragment_dialog_quantity_expression_OK

        buttonOk.setOnClickListener{okButtonClicked()}

        val builder=AlertDialog.Builder(requireActivity())
        builder.setTitle("Quantity expression")
        builder.setView(view)
        return builder.create()

    }

    private fun okButtonClicked(){
        val pattern=PatternChecker(enteredText.text.toString().trim())
        if(pattern.isValid) {
            if(dispatchResult())
                dismiss()
        }else
            toast("Invalid expression. Please try again")
    }

    private fun dispatchResult():Boolean{
        val expression=enteredText.text.toString().trim()
        var callback:QuantityExpressionListener?=null
        try{
            callback = when{
                parentFragment!=null->{
                    parentFragment as QuantityExpressionListener
                }
                activity!=null->{
                    activity as QuantityExpressionListener
                }
                else->{
                    toast("Should be called either from Activity or fragment")
                    null
                }
            }
        }catch(exception:Exception){
            toast("Caller should implement QuantityExpressionListener")
        }

        return if(callback==null)
            false
        else {
            callback.onNewQuantityExpression(expression)
            true
        }
    }

    interface QuantityExpressionListener{
        fun onNewQuantityExpression(expression:String)
    }
}
package com.papco.sundar.papcojoballotment.common

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

class ConfirmationDialog:DialogFragment() {

  companion object{
    const val KEY_CONFIRMATION_ID="key_confirmation_id"
    const val DEFAULT_MSG="Sure want to confirm?"
    const val DEFAULT_POSITIVE_TEXT="CONFIRM"
    const val KEY_MSG="Key_message"
    const val KEY_POSITIVE_TEXT="positive_text"
    const val DEFAULT_ID=-1
    const val TAG="PapcoJobAllotment:ConfirmationDialog"

    fun getInstance(msg:String,positiveButtonText:String,confirmId:Int): ConfirmationDialog {
      val args=Bundle()
      args.putString(KEY_MSG,msg)
      args.putString(KEY_POSITIVE_TEXT,positiveButtonText)
      args.putInt(KEY_CONFIRMATION_ID,confirmId)
      return ConfirmationDialog()
        .also{it.arguments=args}
    }
  }

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    val builder=AlertDialog.Builder(requireContext())
    builder.setMessage(getMessage())
    builder.setPositiveButton(getPositiveText()){_,_->
      dispatchConfirmation()
    }
    builder.setNegativeButton("CANCEL",null)
    return builder.create()
  }

  private fun dispatchConfirmation() {
    try{
      when{
        parentFragment!=null->{
          (parentFragment as ConfirmationDialogListener).onConfirmationDialogConfirm(getConfirmationId())
          return
        }
        activity!=null->{
          (activity as ConfirmationDialogListener).onConfirmationDialogConfirm(getConfirmationId())
          return
        }
      }
    }catch (exception: Exception){
      toast("The caller should implement ConfirmationDialogListener")
    }
  }

  private fun getMessage():String{
    arguments?.let{return it.getString(
      KEY_MSG,
      DEFAULT_MSG
    )}
    return DEFAULT_MSG
  }

  private fun getPositiveText():String{
    arguments?.let{return it.getString(
      KEY_POSITIVE_TEXT,
      DEFAULT_POSITIVE_TEXT
    )}
    return DEFAULT_POSITIVE_TEXT
  }

  private fun getConfirmationId():Int{
    arguments?.let{return it.getInt(
      KEY_CONFIRMATION_ID,
      DEFAULT_ID
    )}
    return DEFAULT_ID
  }

  interface ConfirmationDialogListener{
    fun onConfirmationDialogConfirm(confirmationId:Int)
  }
}
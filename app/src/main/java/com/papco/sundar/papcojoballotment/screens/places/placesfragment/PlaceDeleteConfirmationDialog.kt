package com.papco.sundar.papcojoballotment.screens.places.placesfragment

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.papco.sundar.papcojoballotment.common.toast

class PlaceDeleteConfirmationDialog:DialogFragment() {

    companion object{

        const val KEY_PLACE_ID="place_id"
        const val KEY_PLACE_NAME="place_name"
        const val DEFAULT_ID="NO_ID"
        const val DEFAULT_NAME="NO_NAME"
        const val TAG="PapcoJobAllotment:PlaceDeleteConfirmationDialog"

        fun getInstance(placeId:String,placeName:String): PlaceDeleteConfirmationDialog {
            val args=Bundle()
            args.putString(KEY_PLACE_ID,placeId)
            args.putString(KEY_PLACE_NAME,placeName)
            return PlaceDeleteConfirmationDialog()
                .also { it.arguments=args }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val builder=AlertDialog.Builder(requireActivity())
        builder.setTitle("Delete")
        builder.setMessage("Sure want to delete machine ${getPlaceName()}")
        builder.setPositiveButton("DELETE"){dialog, which ->
            onDeleteConfirmation()
        }
        builder.setNegativeButton("CANCEL",null)
        return builder.create()
    }

    private fun onDeleteConfirmation() {
        dispatchResult(getPlaceId())
        dismiss()
    }


    fun getPlaceId():String{
        arguments?.let{
            return it.getString(
                KEY_PLACE_ID,
                DEFAULT_ID
            )
        }

        return DEFAULT_ID
    }

    fun getPlaceName():String{
        arguments?.let{
            return it.getString(
                KEY_PLACE_NAME,
                DEFAULT_NAME
            )
        }

        return DEFAULT_NAME
    }

    fun dispatchResult(result:String){

        try{
            when{
                parentFragment!=null->{
                    (parentFragment as DeletePlaceConfirmationListener).onDeletePlaceConfirm(result)
                    return
                }
                activity!=null->{
                    (activity as DeletePlaceConfirmationListener).onDeletePlaceConfirm(result)
                    return
                }
                else->{
                    toast("Need to call from either Activity or Fragment")
                }
            }
        }catch (exception:Exception){
            toast("Caller should implement DeletePlaceConfirmation Interface")
        }

    }

    interface DeletePlaceConfirmationListener{
        fun onDeletePlaceConfirm(placeId:String)
    }
}
package com.papco.sundar.papcojoballotment.screens.places.placesfragment

import android.app.Dialog
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.papco.sundar.papcojoballotment.R
import com.papco.sundar.papcojoballotment.common.toast
import com.papco.sundar.papcojoballotment.documents.Place
import kotlinx.android.synthetic.main.fragment_dialog_places.view.*

class PlaceAddFragment:DialogFragment() {


    companion object{

        const val KEY_PLACE_ID="place_id"
        const val KEY_PLACE_NAME="place_name"
        const val DEFAULT_ID="NO_ID"
        const val DEFAULT_NAME="NO_NAME"
        const val TAG="PapcoJobAllotment:AddPlaceDialog"

        fun getEditModeInstance(placeId:String,placeName:String): PlaceAddFragment {
            val args=Bundle()
            args.putString(KEY_PLACE_ID,placeId)
            args.putString(KEY_PLACE_NAME,placeName)
            return PlaceAddFragment()
                .also {it.arguments=args}
        }
    }

    private lateinit var enteredText:EditText
    private lateinit var buttonOk:TextView

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val inflater=requireActivity().layoutInflater
        val view=inflater.inflate(R.layout.fragment_dialog_places,null,false)

        enteredText=view.fragment_dialog_places_name
        buttonOk=view.fragment_dialog_places_OK

        if(isEditMode())
            enteredText.setText(getPlaceName())

        buttonOk.setOnClickListener{okButtonClicked()}

        val builder=AlertDialog.Builder(requireActivity())
        builder.setTitle("Machine name")
        builder.setView(view)
        return builder.create()

    }

    private fun okButtonClicked() {

        if(!isValidEntry(enteredText.text.toString())){
            toast("Please enter a valid machine name")
            return
        }

        if(isEditMode())
            dispatchUpdatePlace(Place(getPlaceId(),enteredText.text.toString()))
        else
            dispatchAddPlace(enteredText.text.toString())

        dialog?.dismiss()

    }

    private fun isValidEntry(desc:String):Boolean{

        if(desc.equals("pool",ignoreCase = true))
            return false

        if(desc.equals("completed",true))
            return false

        if(desc.isBlank())
            return false

        return true
    }

    private fun isEditMode():Boolean{
        arguments?.let { return true }
        return false
    }

    private fun getPlaceId():String{
        var id:String?=
            DEFAULT_ID
        if(isEditMode())
            id=arguments?.getString(
                KEY_PLACE_ID,
                DEFAULT_ID
            )

        id?.let { return it }
        return DEFAULT_ID
    }

    private fun getPlaceName():String{
        var id:String?=
            DEFAULT_NAME
        if(isEditMode())
            id=arguments?.getString(
                KEY_PLACE_NAME,
                DEFAULT_NAME
            )

        id?.let { return it }
        return DEFAULT_NAME
    }

    private fun dispatchAddPlace(placeName:String){
        try{
            when{
                parentFragment!=null->{
                    (parentFragment as PlaceAddUpdateListener).onPlaceAdd(placeName)
                    return
                }
                activity!=null->{
                    (activity as PlaceAddUpdateListener).onPlaceAdd(placeName)
                    return
                }
                else->{
                    toast("Should be called either from Activity or fragment")
                }
            }
        }catch(exception:Exception){
            toast("Called should implement PlaceAddUpdateListener")
        }
    }

    private fun dispatchUpdatePlace(place:Place){
        try{
            when{
                parentFragment!=null->{
                    (parentFragment as PlaceAddUpdateListener).onPlaceUpdate(place)
                    return
                }
                activity!=null->{
                    (activity as PlaceAddUpdateListener).onPlaceUpdate(place)
                    return
                }
                else->{
                    toast("Should be called either from Activity or fragment")
                }
            }
        }catch(exception:Exception){
            toast("Called should implement PlaceAddUpdateListener")
        }
    }

    interface PlaceAddUpdateListener{
        fun onPlaceAdd(placeName:String)
        fun onPlaceUpdate(place:Place)
    }
}
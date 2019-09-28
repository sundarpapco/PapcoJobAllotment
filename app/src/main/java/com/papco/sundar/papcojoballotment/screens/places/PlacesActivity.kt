package com.papco.sundar.papcojoballotment.screens.places

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.papco.sundar.papcojoballotment.R
import com.papco.sundar.papcojoballotment.screens.places.placesfragment.PlaceFragment

class PlacesActivity : AppCompatActivity(){

    companion object {

        const val KEY_START_MODE = "starting_mode"
        const val MODE_SELECT = "select_mode"
        const val KEY_RESULT_PLACE_SELECTED = "place_selected"

        fun getSelectModeIntent(context: Context): Intent {
            val args = Bundle()
            args.putString(KEY_START_MODE, MODE_SELECT)
            val startingIntent = Intent(context, PlacesActivity::class.java)
            startingIntent.putExtras(args)
            return startingIntent
        }

        fun selectedPlaceFromIntent(data:Intent?):String?{

            return data?.extras?.getString(KEY_RESULT_PLACE_SELECTED)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (savedInstanceState == null)
            loadInitialFragment()
    }

    private fun loadInitialFragment(){
        if(isSelectMode())
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, PlaceFragment.getSelectModeInstance())
                .commit()
        else
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, PlaceFragment())
                .commit()
    }


    private fun isSelectMode(): Boolean {
        val args = intent.extras
        args?.let {
            return it.getString(KEY_START_MODE, "null") == MODE_SELECT
        }
        return false
    }


}
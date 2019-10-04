package com.papco.sundar.papcojoballotment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.papco.sundar.papcojoballotment.common.isPrinterVersionApp
import com.papco.sundar.papcojoballotment.screens.home.HomeFragment
import com.papco.sundar.papcojoballotment.screens.places.PlacesActivity
import com.papco.sundar.papcojoballotment.screens.pool.PoolFragment
import com.papco.sundar.papcojoballotment.utility.Duration

class MainActivity : AppCompatActivity() {

    companion object{
        const val REQUEST_PICK_PLACE_FOR_ALLOTMENT=1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        launchInPrinterVersionIfNecessary()
        setContentView(R.layout.activity_main)
        if (savedInstanceState == null)
            loadHomeFragment()
    }

    private fun launchInPrinterVersionIfNecessary() {

        if(isPrinterVersionApp()) {
            val intent = Intent(this, PlacesActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        return when (item?.itemId) {
            android.R.id.home -> {
                supportFragmentManager.popBackStack()
                true
            }
            else->{return false}
        }
    }

    private fun loadHomeFragment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, HomeFragment())
            .commit()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode==Activity.RESULT_OK){
            when(requestCode){
                REQUEST_PICK_PLACE_FOR_ALLOTMENT->{
                    PlacesActivity.selectedPlaceFromIntent(data)
                        ?.let{dispatchPlaceIdToPoolFragment(it)}
                }
            }
        }
    }

    private fun dispatchPlaceIdToPoolFragment(placeId:String) {
        supportFragmentManager.findFragmentByTag(PoolFragment.TAG)?.let {
            (it as PoolFragment).onAllotConfirm(placeId)
        }
    }
}

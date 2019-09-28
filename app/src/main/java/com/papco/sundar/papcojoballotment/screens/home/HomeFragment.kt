package com.papco.sundar.papcojoballotment.screens.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.papco.sundar.papcojoballotment.R
import com.papco.sundar.papcojoballotment.common.disableUpNavigation
import com.papco.sundar.papcojoballotment.common.setSubTitle
import com.papco.sundar.papcojoballotment.common.setTitle
import com.papco.sundar.papcojoballotment.documents.Place
import com.papco.sundar.papcojoballotment.screens.places.placesfragment.PlaceFragment
import com.papco.sundar.papcojoballotment.screens.pool.PoolFragment
import kotlinx.android.synthetic.main.fragment_home.*

class HomeFragment:Fragment() {

    private val viewModel:HomeFragmentVM by lazy{
        ViewModelProviders.of(this).get(HomeFragmentVM::class.java)
    }

    private var unallocatedView:View?=null
    private var machinesView:View?=null
    private var rootView:View?=null
    private var freshViewInflated:Boolean=true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return getSavedInstanceView(inflater,container)


    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if(freshViewInflated)
            initializeView(view)
    }

    private fun getSavedInstanceView(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): View?{

        if(rootView==null) {
            rootView = inflater.inflate(R.layout.fragment_home, container, false)
            freshViewInflated=true
        }else
            freshViewInflated=false

        return rootView

    }

    private fun initializeView(view:View?){
       unallocatedView=view?.findViewById(R.id.unallotted_view)
        machinesView=view?.findViewById(R.id.machines_view)

        machinesView?.setOnClickListener{
            launchManagePlaceScreen()
        }

        unallocatedView?.setOnClickListener{
            launchPoolScreen()
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.totalDocumentLive.observe(viewLifecycleOwner, Observer{
            updateTotalDetails(it)
        })

        viewModel.machinesDocumentLive.observe(viewLifecycleOwner, Observer {
            updateMachineDetails(it)
        })

        viewModel.poolDocumentLive.observe(viewLifecycleOwner, Observer {
            updatePoolDetails(it)
        })
    }

    override fun onResume() {
        super.onResume()
        setTitle("Papco Jobs")
        setSubTitle("")
        disableUpNavigation()
    }

    private fun launchManagePlaceScreen() {

        fragmentManager?.beginTransaction()?.addToBackStack("ToManagePlaces")
            ?.setCustomAnimations(R.anim.enter_from_right,
                R.anim.exit_to_left,R.anim.enter_from_left,R.anim.exit_to_right)
            ?.replace(R.id.container,PlaceFragment(),PlaceFragment.TAG)
            ?.commit()
    }

    private fun launchPoolScreen() {
        fragmentManager?.beginTransaction()?.addToBackStack("ToPoolScreen")
            ?.setCustomAnimations(R.anim.enter_from_right,
                R.anim.exit_to_left,R.anim.enter_from_left,R.anim.exit_to_right)
            ?.replace(R.id.container,PoolFragment(),PoolFragment.TAG)
            ?.commit()
    }

    private fun updateMachineDetails(place: Place){
        machine_job_count.text="${place.jobCount} Jobs"
        machine_time.text=place.duration.asDetailString()
        machine_days_count.text=place.duration.daysOfWork(11)
    }

    private fun updatePoolDetails(place: Place){
        unalloted_job_count.text="${place.jobCount} Jobs"
        unalloted_time.text=place.duration.asDetailString()
        unalloted_days_count.text=place.duration.daysOfWork(11)
    }

    private fun updateTotalDetails(place: Place){
        total_job_count.text="${place.jobCount} Jobs"
        total_time.text=place.duration.asDetailString()
        total_days_count.text=place.duration.daysOfWork(11)
    }

}
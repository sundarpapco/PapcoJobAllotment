package com.papco.sundar.papcojoballotment.screens.places.placesfragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.papco.sundar.papcojoballotment.R
import com.papco.sundar.papcojoballotment.common.*
import com.papco.sundar.papcojoballotment.documents.Place
import com.papco.sundar.papcojoballotment.screens.places.PlacesActivity
import com.papco.sundar.papcojoballotment.screens.places.machineFragment.MachineFragment
import com.papco.sundar.papcojoballotment.utility.EventMessage
import kotlinx.android.synthetic.main.fragment_places.*
import kotlinx.android.synthetic.main.loading_bar.*

class PlaceFragment:Fragment(),
    PlacesAdapter.PlacesAdapterListener,
    PlaceDeleteConfirmationDialog.DeletePlaceConfirmationListener,
    PlaceAddFragment.PlaceAddUpdateListener {

    companion object {

        const val KEY_START_MODE = "starting_mode"
        const val MODE_SELECT = "select_mode"
        const val TAG="PapcoJobAllotment:PlaceFragment"

        fun getSelectModeInstance(): PlaceFragment {
            val args = Bundle()
            args.putString(
                KEY_START_MODE,
                MODE_SELECT
            )
            return PlaceFragment()
                .also { it.arguments=args }
        }
    }

    private val viewModel: PlacesFragmentVM by lazy {
        ViewModelProviders.of(requireActivity()).get(PlacesFragmentVM::class.java)
    }

    private val adapter: PlacesAdapter by lazy {
        PlacesAdapter(
            requireActivity(),
            this
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_places,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initViewModelObservers()
    }

    private fun initViewModelObservers() {
        viewModel.eventBus.observe(viewLifecycleOwner, Observer<EventMessage> { event ->

            if (!event.isAlreadyHandled)
                handleMessage(event)
        })

        viewModel.placesList.observe(viewLifecycleOwner, Observer<List<Place>> {

            adapter.changeData(it)
            hideLoadingProgressBar()

        })
    }

    override fun onResume() {
        super.onResume()
        enableUpNavigation()
        if (isSelectMode())
            setTitle("Select machine")
        else
            setTitle("Machines")
        setSubTitle("")
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if(item.itemId==android.R.id.home){
            activity?.finish()
            return true
        }

        return false
    }


    private fun initViews() {
        if (isSelectMode() || isPrinterVersionApp())
            fragment_places_fab.hide()
        else
            fragment_places_fab.setOnClickListener {
                showAddPlaceDialog()
            }


        initRecycler()
    }

    private fun initRecycler() {
        recycler_places.layoutManager = LinearLayoutManager(requireContext())
        recycler_places.addItemDecoration(SpacingDecoration(requireContext()))
        recycler_places.adapter = adapter
        showLoadingProgressBar()
    }

    private fun showAddPlaceDialog() {
        PlaceAddFragment().show(childFragmentManager,
            PlaceAddFragment.TAG
        )
    }

    private fun showEditPlaceDialog(placeId: String, placeName: String) {

        PlaceAddFragment.getEditModeInstance(
            placeId,
            placeName
        )
            .show(childFragmentManager,
                PlaceAddFragment.TAG
            )
    }

    private fun handleMessage(event: EventMessage) {
        when (event.msgType) {
            EventMessage.EVENT_TRANSACTION_START -> {
                event.isAlreadyHandled = true
                showWaitDialog(event.data as String)
            }
            EventMessage.EVENT_TRANSACTION_END -> {
                event.isAlreadyHandled = true
                hideWaitDialog()
            }
        }
    }

    private fun showLoadingProgressBar() {
        loading_progress_bar.visibility = View.VISIBLE
    }

    private fun hideLoadingProgressBar() {
        loading_progress_bar.visibility = View.GONE
    }

    private fun showWaitDialog(waitMsg: String) {
        WaitDialog.getInstance(waitMsg).show(childFragmentManager, WaitDialog.TAG_FRAGMENT)
    }

    private fun hideWaitDialog() {
        val waitFragment: DialogFragment? =
            childFragmentManager.findFragmentByTag(WaitDialog.TAG_FRAGMENT) as DialogFragment
        waitFragment?.dismiss()
    }

    private fun selectPlace(place: Place) {
        val data = Intent()
        data.putExtra(PlacesActivity.KEY_RESULT_PLACE_SELECTED, place.id)
        activity?.let {
            it.setResult(Activity.RESULT_OK, data)
            it.finish()
        }
    }

    private fun showDeletePlaceConfirmation(place: Place) {

        PlaceDeleteConfirmationDialog.getInstance(
            place.id,
            place.name
        )
            .show(childFragmentManager,
                PlaceDeleteConfirmationDialog.TAG
            )

    }

    private fun showMachineFragment(placeId: String) {

        fragmentManager?.beginTransaction()?.addToBackStack("ToMachineFragment")
            ?.setCustomAnimations(R.anim.enter_from_right,
                R.anim.exit_to_left,R.anim.enter_from_left,R.anim.exit_to_right)
            ?.replace(R.id.container,MachineFragment.getInstance(placeId),MachineFragment.TAG)
            ?.commit()

    }

    override fun onPlaceClicked(place: Place) {

        if (isSelectMode())
            selectPlace(place)
        else
            showMachineFragment(place.id)
    }


    override fun onPlaceLongClicked(view: View, place: Place) {

        if(isPrinterVersionApp())
            return

        val popupMenu = PopupMenu(requireContext(), view)
        requireActivity().menuInflater.inflate(R.menu.menu_delete, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener {

            if (it.itemId == R.id.action_delete) {
                showDeletePlaceConfirmation(place)
                return@setOnMenuItemClickListener true
            }

            if(it.itemId==R.id.action_edit_machine_name){
                showEditPlaceDialog(place.id,place.name)
                return@setOnMenuItemClickListener true
            }

            return@setOnMenuItemClickListener false
        }
        popupMenu.show()
    }

    override fun onDeletePlaceConfirm(placeId: String) {
        viewModel.deletePlace(placeId)
    }

    override fun onPlaceAdd(placeName: String) {
        viewModel.addPlace(placeName)
    }

    override fun onPlaceUpdate(place: Place) {
        viewModel.updatePlace(place.id, place.name)
    }


    private fun isSelectMode(): Boolean {
        arguments?.let {
            return it.getString(KEY_START_MODE, "null") == MODE_SELECT
        }
        return false
    }
}
package com.papco.sundar.papcojoballotment.screens.pool

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.papco.sundar.papcojoballotment.MainActivity
import com.papco.sundar.papcojoballotment.R
import com.papco.sundar.papcojoballotment.common.*
import com.papco.sundar.papcojoballotment.documents.PrintJob
import com.papco.sundar.papcojoballotment.screens.job.JobFragment
import com.papco.sundar.papcojoballotment.screens.places.PlacesActivity
import com.papco.sundar.papcojoballotment.utility.EventMessage
import kotlinx.android.synthetic.main.fragment_pool.*

class PoolFragment : Fragment(),
    JobsAdapterListener,
    ConfirmationDialog.ConfirmationDialogListener {

    companion object {
        const val TAG = "PapcoJobAllotment:PoolFragment"
    }

    private var rootView: View? = null
    private var freshViewInflated = true

    private val viewModel: PoolFragmentVM by lazy {
        ViewModelProviders.of(this).get(PoolFragmentVM::class.java)
    }

    private val adapter: JobsAdapter by lazy {
        JobsAdapter(requireActivity(), this, viewModel.jobsSelection)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return getSavedInstanceView(inflater, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (freshViewInflated)
            initView()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.poolDocumentWatcher.observe(viewLifecycleOwner, Observer {
            setSubTitle("${it.duration.asString()} in ${it.jobCount} jobs")
        })

        viewModel.eventBus.observe(viewLifecycleOwner, Observer {
            if (it.isAlreadyHandled)
                return@Observer

            handleMessage(it)
        })

        viewModel.jobsList.observe(viewLifecycleOwner, Observer {
            adapter.changeData(it)
        })
    }


    override fun onResume() {
        super.onResume()
        enableUpNavigation()
        setTitle("Unalloted jobs")
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.jobsSelection = adapter.jobsSelection
    }

    private fun initRecycler() {
        val dragHelper = ItemTouchHelper(ItemTouchHelperCallBack(adapter))
        adapter.dragHelper = dragHelper
        recycler_jobs.addItemDecoration(SpacingDecoration(requireContext()))
        recycler_jobs.layoutManager = LinearLayoutManager(requireContext())
        recycler_jobs.adapter = adapter
        dragHelper.attachToRecyclerView(recycler_jobs)
    }

    private fun getSavedInstanceView(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): View? {

        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_pool, container, false)
            freshViewInflated = true
        } else
            freshViewInflated = false

        return rootView
    }

    private fun initView() {

        fragment_pool_fab.setOnClickListener {
            showAddNewJobFragment()
        }

        initRecycler()

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
                if (!event.isSuccess)
                    toast(event.data as String)
            }
        }

    }

    private fun showWaitDialog(waitMsg: String) {
        WaitDialog.getInstance(waitMsg).show(childFragmentManager, WaitDialog.TAG_FRAGMENT)
    }

    private fun hideWaitDialog() {
        val waitDialog = childFragmentManager.findFragmentByTag(WaitDialog.TAG_FRAGMENT)
        waitDialog?.let { (it as WaitDialog).dismiss() }
    }

    private fun showAddNewJobFragment() {
        fragmentManager?.beginTransaction()?.addToBackStack("ToAddJobFragment")
            ?.replace(R.id.container, JobFragment(), JobFragment.TAG)?.commit()
    }

    private fun showEditJobFragment(job: PrintJob) {
        fragmentManager?.beginTransaction()?.addToBackStack("ToEditJobFragment")
            ?.replace(R.id.container, JobFragment.editModeInstance(job.id), JobFragment.TAG)
            ?.commit()
    }

    override fun onJobClicked(job: PrintJob) {
        showEditJobFragment(job)
    }

    override fun onActionModeStart() {
        fragment_pool_fab.hide()
    }

    override fun onActionModeStop() {
        fragment_pool_fab.show()
    }

    override fun onDeleteJobs(jobIds: MutableSet<String>) {

        ConfirmationDialog.
            getInstance("Sure want to delete ${jobIds.size} jobs?","DELETE",1)
            .show(childFragmentManager,ConfirmationDialog.TAG)
    }

    override fun onJobMoved(movedJob: PrintJob) {
        viewModel.moveJob(movedJob)
    }

    override fun onConfirmationDialogConfirm(confirmationId: Int) {
        viewModel.deleteJobs(adapter.jobsSelection.keys())
    }

    override fun onAllotJobs(jobIds: MutableSet<String>) {
        val intent = PlacesActivity.getSelectModeIntent(requireActivity())
        requireActivity().startActivityForResult(
            intent,
            MainActivity.REQUEST_PICK_PLACE_FOR_ALLOTMENT
        )
    }

    //this function will be called by the activity when it got the result from place picker activity
    fun onAllotConfirm(placeId: String) {
        viewModel.allotJobs(placeId, adapter.jobsSelection.keys())
    }

}
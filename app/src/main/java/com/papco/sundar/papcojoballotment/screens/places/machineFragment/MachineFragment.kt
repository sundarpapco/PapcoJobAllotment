package com.papco.sundar.papcojoballotment.screens.places.machineFragment

import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.papco.sundar.papcojoballotment.R
import com.papco.sundar.papcojoballotment.common.*
import com.papco.sundar.papcojoballotment.documents.PrintJob
import com.papco.sundar.papcojoballotment.utility.EventMessage
import kotlinx.android.synthetic.main.fragment_machine.*
import kotlinx.android.synthetic.main.loading_bar.*
import java.text.SimpleDateFormat
import java.util.*

class MachineFragment:Fragment(),
    MachineJobsAdapterListener,
    ConfirmationDialog.ConfirmationDialogListener {

    companion object{
        const val KEY_MACHINE_ID="machine_id"
        const val INVALID_ID="invalid_id"
        const val TAG="PapcoJobAllotment:MachineFragment"

        fun getInstance(machineId:String):MachineFragment{

            val arg= Bundle()
            arg.putString(KEY_MACHINE_ID,machineId)
            return MachineFragment().also { it.arguments=arg }
        }
    }

    private val confirmIdCompleteJob=1
    private val confirmIdRemoveJob=2
    private val lastCompletionTextFormat="dd/MM/yyyy, hh:mm a"

    private val viewModel:MachineFragmentVH by lazy{
        ViewModelProviders.of(this).get(MachineFragmentVH::class.java)
    }

    private val adapter:MachineJobsAdapter by lazy{
        val adapter=MachineJobsAdapter(requireActivity(),this)
        if(getPlaceId()!= INVALID_ID)
            viewModel.loadJobs(getPlaceId())
        adapter
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
        return inflater.inflate(R.layout.fragment_machine,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.jobsList.observe(viewLifecycleOwner, Observer {
            adapter.changeData(it)
            hideLoadingProgressBar()
        })

        viewModel.machineDocument.observe(viewLifecycleOwner, Observer {

            if(it==null) {
                fragmentManager?.popBackStack()
                return@Observer
            }else{
                setTitle(it.name)
                setSubTitle("${it.duration} in ${it.jobCount} jobs")
                updateLastCompletionTimeStamp(it.lastCompletion)
            }

        })

        viewModel.eventBus.observe(viewLifecycleOwner, Observer {
            if(it.isAlreadyHandled)
                return@Observer

            handleMessage(it)
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if(item.itemId==android.R.id.home){
            fragmentManager?.popBackStack()
            return true
        }

        return false
    }

    private fun initViews() {
        initRecycler()
    }

    private fun handleMessage(event: EventMessage) {

        when(event.msgType){
            EventMessage.EVENT_TRANSACTION_START->{
                event.isAlreadyHandled=true
                showWaitDialog(event.data as String)
            }
            EventMessage.EVENT_TRANSACTION_END->{
                event.isAlreadyHandled=true
                hideWaitDialog()
                if(!event.isSuccess)
                    toast(event.data as String)
            }
        }

    }

    private fun showWaitDialog(waitMsg:String){
        WaitDialog.getInstance(waitMsg).show(childFragmentManager,WaitDialog.TAG_FRAGMENT)
    }

    private fun hideWaitDialog(){
        val waitDialog=childFragmentManager.findFragmentByTag(WaitDialog.TAG_FRAGMENT)
        waitDialog?.let {(it as WaitDialog).dismiss()}
    }

    private fun initRecycler() {
        val dragHelper= ItemTouchHelper(ItemTouchHelperCallBack(adapter))
        adapter.dragHelper=dragHelper
        recycler_machine_jobs.layoutManager = LinearLayoutManager(requireContext())
        recycler_machine_jobs.addItemDecoration(SpacingDecoration(requireContext()))
        recycler_machine_jobs.adapter=adapter
        showLoadingProgressBar()
        dragHelper.attachToRecyclerView(recycler_machine_jobs)
    }

    private fun showLoadingProgressBar() {
        loading_progress_bar.visibility = View.VISIBLE
    }

    private fun hideLoadingProgressBar() {
        loading_progress_bar.visibility = View.GONE
    }

    private fun showCompleteConfirmationDialog(){
        ConfirmationDialog.getInstance("Mark this job as complete?",
            "COMPLETE",confirmIdCompleteJob)
            .show(childFragmentManager,ConfirmationDialog.TAG)
    }

    private fun showRemoveConfirmationDialog(){
        ConfirmationDialog.getInstance("Remove this job from list?",
            "REMOVE",confirmIdRemoveJob)
            .show(childFragmentManager,ConfirmationDialog.TAG)
    }

    override fun onConfirmationDialogConfirm(confirmationId: Int) {
        when(confirmationId){
            confirmIdCompleteJob->{
                viewModel.completeJob()
            }
            confirmIdRemoveJob->{
                viewModel.removeJobFromQueue()
            }
        }
    }

    private fun getPlaceId():String{
        arguments?.let { return it.getString(KEY_MACHINE_ID, INVALID_ID) }
        return INVALID_ID
    }

    private fun updateLastCompletionTimeStamp(completionTime:Long){

        when(completionTime){
            0L->{
                last_completion_text.text=requireActivity()
                    .getString(R.string.default_last_completion)
            }
            else->{
                last_completion_text.text=requireActivity().let {
                    String.format(it.getString(R.string.last_completion_format),
                        SimpleDateFormat(lastCompletionTextFormat,Locale.getDefault())
                            .format(Date(completionTime)))
                }
            }
        }
    }

    private fun completeJob(job:PrintJob){
        viewModel.selectedJob=job
        showCompleteConfirmationDialog()
    }

    private fun removeJobFromQueue(job:PrintJob){
        viewModel.selectedJob=job
        showRemoveConfirmationDialog()
    }

    //region ------ Adapter callbacks

    override fun onJobClicked(job: PrintJob) {

    }

    override fun onJobLongClicked(position:Int, view: View, job: PrintJob) {

        if(isPrinterVersionApp() && position!=0)
            return

        val popup=PopupMenu(requireContext(),view)

        popup.menuInflater.inflate(R.menu.menu_machine_actions,popup.menu)

        if(isPrinterVersionApp())
            popup.menu.findItem(R.id.action_remove_job_from_queue).isVisible = false

        popup.setOnMenuItemClickListener {
            when(it.itemId){
                R.id.action_complete_job->{
                    completeJob(job)
                    true
                }
                R.id.action_remove_job_from_queue->{
                    removeJobFromQueue(job)
                    true
                }
                else->{
                    false
                }
            }
        }
        popup.show()
    }

    override fun onJobMoved(movedJob: PrintJob) {
        viewModel.moveJob(movedJob)
    }

    //endregion

}
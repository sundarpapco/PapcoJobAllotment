package com.papco.sundar.papcojoballotment.screens.job

import android.app.Activity
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.papco.sundar.papcojoballotment.R
import com.papco.sundar.papcojoballotment.common.*
import com.papco.sundar.papcojoballotment.documents.PrintJob
import com.papco.sundar.papcojoballotment.utility.Duration
import com.papco.sundar.papcojoballotment.utility.EventMessage
import com.papco.sundar.papcojoballotment.utility.PatternChecker
import kotlinx.android.synthetic.main.fragment_job.*
import kotlinx.android.synthetic.main.fragment_job.view.*

class JobFragment : Fragment(), DatePickerFragment.DatePickerDialogListener,
    QuantityExpFragment.QuantityExpressionListener {


    companion object {
        const val TAG = "PapcoJobAllotment:JobFragment"
        const val KEY_JOB_ID = "JobFragment:KeyJobId"

        fun editModeInstance(jobId: String): JobFragment {

            val args = Bundle()
            args.putString(KEY_JOB_ID, jobId)
            return JobFragment().also { it.arguments = args }

        }
    }

    private var spotColourMakereadyIncluded:Boolean=false
    private val KEY_SPOT_COLOR_MAKE_READY="spot_color_status"

    private val viewModel: JobFragmentVM by lazy {
        val viewModel = ViewModelProviders.of(this).get(JobFragmentVM::class.java)
        getJobId()?.let { viewModel.loadJob(it) }
        viewModel
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        restoreSavedState(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_job, container, false)
        view.fragment_job_date.setText(DatePickerFragment.now())
        view.fragment_job_date.setOnClickListener {
            showDatePickerDialog((it as TextView).text.toString())
        }
        view.fragment_job_expression_image.setOnClickListener {
            showQuantityExpressionDialog()
        }
        return view
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(KEY_SPOT_COLOR_MAKE_READY,spotColourMakereadyIncluded)
    }

    private fun restoreSavedState(savedInstanceState: Bundle?){
        spotColourMakereadyIncluded=savedInstanceState?.getBoolean(KEY_SPOT_COLOR_MAKE_READY)?:false
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.eventBus.observe(viewLifecycleOwner, Observer { event ->

            if (event.isAlreadyHandled)
                return@Observer
            else
                handleMessage(event)
        })

    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_done, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {

            R.id.action_done -> {
                onActionDoneClick()
                true
            }

            else -> {
                false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        initActionBar()
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
                if (event.isSuccess)
                    fragmentManager?.popBackStack()
            }

            EventMessage.EVENT_LOAD_JOB -> {
                event.isAlreadyHandled = true
                loadJob(event.data as PrintJob)
            }

            EventMessage.EVENT_JOB_DELETED -> {
                event.isAlreadyHandled = true
                fragmentManager?.popBackStack()
            }

        }

    }

    private fun onActionDoneClick() {
        isAllFieldsValid()?.let { okJob ->

            if (getJobId() == null)
                viewModel.addNewJob(okJob)
            else {
                viewModel.updateJob(okJob)
            }
            hideKeyBoard()
        }
    }

    private fun initActionBar() {
        setSubTitle("")
        enableUpNavigation()
        if (isEditMode())
            setTitle("Edit Job")
        else
            setTitle("Add Job")
    }

    private fun showWaitDialog(waitMsg: String) {
        fragmentManager?.let {
            WaitDialog.getInstance(waitMsg).show(it, WaitDialog.TAG_FRAGMENT)
        }

    }

    private fun showQuantityExpressionDialog() {
        QuantityExpFragment().show(childFragmentManager, QuantityExpFragment.TAG)
    }

    private fun hideWaitDialog() {
        fragmentManager?.let {
            val waitFragment: DialogFragment? =
                it.findFragmentByTag(WaitDialog.TAG_FRAGMENT) as DialogFragment
            waitFragment?.let { fragment ->
                fragment.dialog?.dismiss()
            }
        }
    }

    private fun showDatePickerDialog(dateString: String) {

        DatePickerFragment.startWithDate(dateString)
            .show(childFragmentManager, DatePickerFragment.TAG)
    }


    private fun isEditMode(): Boolean {
        arguments?.let { return true }
        return false
    }

    private fun getJobId(): String? {

        arguments?.let {
            return it.getString(KEY_JOB_ID)
        }
        return null
    }

    private fun isAllFieldsValid(): PrintJob? {

        val resultJob = PrintJob()
        var resultString = ""

        //PO Number
        var checkingString = fragment_job_po_number.text.toString()
        if (checkingString.isBlank() || checkingString.toInt() <= 0) {
            resultString += "Enter valid PO Number\n"
        } else
            resultJob.poNumber = checkingString

        //Client Name
        checkingString = fragment_job_client.text.toString().trim()
        if (checkingString.isBlank()) {
            resultString += "Enter valid Client name\n"
        } else
            resultJob.client = checkingString

        //Paper details
        checkingString = fragment_job_paper.text.toString().trim()
        if (checkingString.isBlank()) {
            resultString += "Enter valid Paper details\n"
        } else
            resultJob.paper = checkingString

        //Color
        checkingString = fragment_job_color.text.toString().trim()
        if (checkingString.isBlank()) {
            resultString += "Enter valid Color details\n"
        } else
            resultJob.color = checkingString

        //Hour and Minute
        var hours = 0
        var minutes = 0
        checkingString = fragment_job_hours.text.toString()
        if (!checkingString.isBlank()) {
            hours = checkingString.toInt()
        }

        checkingString = fragment_job_minutes.text.toString()
        if (!checkingString.isBlank()) {
            minutes = checkingString.toInt()
        }

        if (hours == 0 && minutes == 0)
            resultString += "Enter valid running time\n"
        else
            resultJob.runningTime = Duration(hours, minutes)

        resultJob.pendingReason = fragment_job_pending.text.toString().trim()
        resultJob.spotColourMakeReady=spotColourMakereadyIncluded

        return when {
            resultString.isBlank() -> {
                resultJob.date = fragment_job_date.text.toString()
                if (urgent_switch.isChecked)
                    resultJob.isUrgent = true
                resultJob
            }
            else -> {
                toast(resultString)
                null
            }
        }
    }

    private fun loadJob(job: PrintJob) {

        fragment_job_date.setText(job.date)
        fragment_job_po_number.setText(job.poNumber)
        fragment_job_client.setText(job.client)
        fragment_job_paper.setText(job.paper)
        fragment_job_color.setText(job.color)
        fragment_job_hours.setText(job.runningTime.hours.toString())
        fragment_job_minutes.setText(job.runningTime.minutes.toString())
        fragment_job_pending.setText(job.pendingReason)
        if (job.isUrgent)
            urgent_switch.isChecked = true
        spotColourMakereadyIncluded = job.spotColourMakeReady

    }

    override fun onDateSet(date: String) {
        fragment_job_date.setText(date)
    }

    override fun onNewQuantityExpression(expression: String) {
        val pattern=PatternChecker(expression)
        val duration=pattern.totalTime()
        fragment_job_hours.setText(duration.hours.toString())
        fragment_job_minutes.setText(duration.minutes.toString())
        spotColourMakereadyIncluded=pattern.hasExtraColour
    }

    private fun hideKeyBoard() {
        val imm =
            requireActivity().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(fragment_job_po_number.windowToken, 0)
    }


}
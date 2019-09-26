package com.papco.sundar.papcojoballotment.screens.pool

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ListUpdateCallback
import androidx.recyclerview.widget.RecyclerView
import com.papco.sundar.papcojoballotment.R
import com.papco.sundar.papcojoballotment.common.ItemTouchHelperCallBack
import com.papco.sundar.papcojoballotment.documents.PrintJob
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class JobsAdapter(
    activity: FragmentActivity,
    private val callback: JobsAdapterListener,
    var jobsSelection: JobsSelection = JobsSelection()
) : RecyclerView.Adapter<JobsAdapter.JobsAdapterVH>(),
    ActionMode.Callback,
    ItemTouchHelperCallBack.DragCallBack {

    private var data: MutableList<PrintJob> = LinkedList()
    private var pendingRedColor:Int
    private var actionMode: ActionMode? = null
    private val coroutineScope = activity.lifecycleScope
    private val diffCallback = DiffUpdateCallBack(this)
    var dragHelper: ItemTouchHelper? = null

    init {
        if (!jobsSelection.isEmpty()) {
            actionMode = activity.startActionMode(this)
            updateActionModeTitle()
        }

        pendingRedColor = if(android.os.Build.VERSION.SDK_INT>=android.os.Build.VERSION_CODES.M){
            activity.getColor(R.color.pendingRed)
        }else{
            activity.resources.getColor(R.color.pendingRed)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobsAdapterVH {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.list_item_job_orange, parent, false)
        return JobsAdapterVH(view)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: JobsAdapterVH, position: Int) {
        holder.bind()
    }

    fun changeData(newData: MutableList<PrintJob>) {
        refreshData(data, newData)
    }

    private fun refreshData(oldData: List<PrintJob>, newData: MutableList<PrintJob>) {

        coroutineScope.launch(Dispatchers.Default) {

            val diffResult = DiffUtil.calculateDiff(
                DiffCallBack(oldData, newData)
            )
            if (isActive) {
                withContext(Dispatchers.Main) {
                    diffCallback.oldData = data
                    data = newData
                    diffResult.dispatchUpdatesTo(diffCallback)
                    diffCallback.oldData=null
                }
            }

        }

    }

    private fun clearList() {

        for (key in jobsSelection.keys()) {
            jobsSelection.get(key)?.let { notifyItemChanged(it) }
        }
        jobsSelection.clear()
    }

    private fun updateActionModeTitle() {
        actionMode?.title = "${jobsSelection.getDuration()}"
    }


    override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {

        if (item == null)
            return false

        return when (item.itemId) {
            R.id.action_allot -> {
                callback.onAllotJobs(jobsSelection.keys())
                true
            }
            R.id.action_delete -> {
                callback.onDeleteJobs(jobsSelection.keys())
                true
            }
            else -> {
                false
            }
        }
    }

    override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        val inflater = mode?.menuInflater
        inflater?.inflate(R.menu.menu_pool_actions, menu)
        callback.onActionModeStart()
        return true
    }

    override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        return false
    }

    override fun onDestroyActionMode(mode: ActionMode?) {
        actionMode = null
        clearList()
        callback.onActionModeStop()
    }

    fun removeFromSelectionIfNecessary(job: PrintJob) {

        if(actionMode==null)
            return

        jobsSelection.remove(job.id, job.runningTime)
        updateActionModeTitle()
        if (jobsSelection.isEmpty())
            actionMode?.finish()

    }

    override fun onDragging(fromPosition: Int, toPosition: Int) {
        val temp = data[fromPosition]
        data[fromPosition] = data[toPosition]
        data[toPosition] = temp
        notifyItemMoved(fromPosition, toPosition)
    }

    override fun onMoved(fromPosition: Int, toPosition: Int) {


        when (toPosition) {

            data.size - 1 -> {
                //Last item. So, place the current time there
                data[toPosition].position = data[toPosition-1].position +1
            }

            0 -> {
                //First item. So, place one day before the immediate below item
                data[toPosition].position = data[toPosition + 1].position - 1
            }

            else -> {
                /*The item is dropped in middle, so the order will be the average time between
                the top and bottom items

                We are using the try catch, because during dragging when the list has only two items
                and one of the items was deleted by someone elsewhere, it will cause out of bounds
                exception since that will be the only one item. So, handing that case with try catch
                 */

                val temp = data[toPosition]
                try {
                    val average =
                        (data[toPosition - 1].position + data[toPosition + 1].position) / 2.0
                    data[toPosition].position = average
                } catch (e: IndexOutOfBoundsException) {
                    data[0] = temp
                }
            }

        }


        callback.onJobMoved(data[toPosition])

    }

    @SuppressLint("ClickableViewAccessibility")
    inner class JobsAdapterVH(view: View) : RecyclerView.ViewHolder(view) {

        private val poNumber: TextView = view.findViewById(R.id.list_item_job_po_number)
        private val clientName: TextView = view.findViewById(R.id.list_item_job_client)
        private val paperDetails: TextView = view.findViewById(R.id.list_item_job_paper)
        private val colorDetails: TextView = view.findViewById(R.id.list_item_job_color)
        private val runningTime: TextView = view.findViewById(R.id.list_item_job_time)
        private val dragHandle: ImageView = view.findViewById(R.id.list_item_job_drag_handle)

        init {
            view.setOnClickListener {
                if (actionMode == null)
                    callback.onJobClicked(data[adapterPosition])
                else
                    toggleSelection()
            }

            view.setOnLongClickListener {
                if (actionMode == null) {
                    actionMode = itemView.startActionMode(this@JobsAdapter)
                    addToSelection()
                }
                true
            }

            dragHandle.setOnTouchListener { _, event ->

                if (actionMode == null && event.action == MotionEvent.ACTION_DOWN) {
                    dragHelper?.startDrag(this@JobsAdapterVH)
                }

                false
            }
        }


        fun bind() {
            updateViews()

            if (actionMode != null)
                addToSelectionIfNecessary()
            else
                itemView.isActivated = false
        }

        private fun updateViews() {
            poNumber.text =
                "PO:${data[adapterPosition].poNumber} (${data[adapterPosition].ageString})"
            clientName.text = data[adapterPosition].client
            paperDetails.text = data[adapterPosition].paper
            colorDetails.text = data[adapterPosition].color
            runningTime.text = data[adapterPosition].runningTime.asString()
            if (data[adapterPosition].pendingReason.isBlank())
                clientName.setTextColor(Color.BLACK)
            else
                clientName.setTextColor(pendingRedColor)
        }

        private fun addToSelection() {
            itemView.isActivated = true
            jobsSelection.add(
                data[adapterPosition].id,
                adapterPosition,
                data[adapterPosition].runningTime
            )
            updateActionModeTitle()
        }

        private fun removeFromSelection() {
            jobsSelection.remove(data[adapterPosition].id, data[adapterPosition].runningTime)
            itemView.isActivated = false
            if (jobsSelection.isEmpty())
                actionMode?.finish()
            else
                updateActionModeTitle()
        }

        private fun toggleSelection() {
            if (itemView.isActivated)
                removeFromSelection()
            else
                addToSelection()
        }

        private fun addToSelectionIfNecessary() {
            if (jobsSelection.contains(data[adapterPosition].id))
                addToSelection()
            else
                itemView.isActivated = false
        }
    }
}


class DiffCallBack(private var oldData: List<PrintJob>, private var newData: List<PrintJob>) :
    DiffUtil.Callback() {


    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldData[oldItemPosition].id == newData[newItemPosition].id
    }

    override fun getOldListSize(): Int {
        return oldData.size
    }

    override fun getNewListSize(): Int {
        return newData.size
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldData[oldItemPosition] == newData[newItemPosition]
    }

}


class DiffUpdateCallBack(private val adapter: JobsAdapter) : ListUpdateCallback {

    var oldData: MutableList<PrintJob>? = null

    override fun onChanged(position: Int, count: Int, payload: Any?) {

        adapter.notifyItemRangeChanged(position, count, payload)
    }

    override fun onMoved(fromPosition: Int, toPosition: Int) {
        adapter.notifyItemMoved(fromPosition, toPosition)
    }

    override fun onInserted(position: Int, count: Int) {

        oldData?.let{
            if(it.size>0 && it.size==position)
                adapter.notifyItemChanged(position-1)
        }

        adapter.notifyItemRangeInserted(position, count)
    }

    override fun onRemoved(position: Int, count: Int) {

        oldData?.let {
            for (itemPosition in position until position + count) {
                adapter.removeFromSelectionIfNecessary(it[itemPosition])
            }

            if(position>0 && (position+count)==it.size){
                adapter.notifyItemChanged(position-1,Boolean)
            }
        }
        adapter.notifyItemRangeRemoved(position, count)
    }

}

interface JobsAdapterListener {
    fun onJobClicked(job: PrintJob)
    fun onActionModeStart()
    fun onActionModeStop()
    fun onDeleteJobs(jobIds: MutableSet<String>)
    fun onAllotJobs(jobIds: MutableSet<String>)
    fun onJobMoved(movedJob:PrintJob)
}
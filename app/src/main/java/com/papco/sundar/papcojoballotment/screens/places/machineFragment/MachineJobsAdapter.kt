package com.papco.sundar.papcojoballotment.screens.places.machineFragment

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
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
import com.papco.sundar.papcojoballotment.screens.pool.DiffCallBack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class MachineJobsAdapter(
  activity: FragmentActivity,
  private val callback: MachineJobsAdapterListener) :
  RecyclerView.Adapter<MachineJobsAdapter.MachineJobsAdapterVH>(),
  ItemTouchHelperCallBack.DragCallBack {

  private var data: MutableList<PrintJob> = LinkedList()
  private val coroutineScope=activity.lifecycleScope
  var dragHelper:ItemTouchHelper?=null
  private val diffCallBack=MachineDiffUpdateCallBack(this)

  private var pendingRedColor:Int = if(android.os.Build.VERSION.SDK_INT>=android.os.Build.VERSION_CODES.M){
    activity.getColor(R.color.pendingRed)
  }else{
    activity.resources.getColor(R.color.pendingRed)
  }


  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MachineJobsAdapterVH {
    val view =
      LayoutInflater.from(parent.context).inflate(R.layout.list_item_job, parent, false)
    return MachineJobsAdapterVH(view)
  }

  override fun getItemCount(): Int {
    return data.size
  }

  override fun onBindViewHolder(holder: MachineJobsAdapterVH, position: Int) {
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
          diffCallBack.oldData=data
          data = newData
          diffResult.dispatchUpdatesTo(diffCallBack)
          diffCallBack.oldData=null
        }
      }

    }

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
  inner class MachineJobsAdapterVH(view: View) : RecyclerView.ViewHolder(view) {

    private val poNumber: TextView = view.findViewById(R.id.list_item_job_po_number)
    private val clientName: TextView = view.findViewById(R.id.list_item_job_client)
    private val paperDetails: TextView = view.findViewById(R.id.list_item_job_paper)
    private val colorDetails: TextView = view.findViewById(R.id.list_item_job_color)
    private val runningTime: TextView = view.findViewById(R.id.list_item_job_time)
    private val dragHandle: ImageView = view.findViewById(R.id.list_item_job_drag_handle)

    init {
      view.setOnClickListener {

      }

      view.setOnLongClickListener{

        callback.onJobLongClicked(adapterPosition,it,data[adapterPosition])
        true
      }

      dragHandle.setOnTouchListener { v, event ->

        if (event.action == MotionEvent.ACTION_DOWN) {
          dragHelper?.startDrag(this@MachineJobsAdapterVH)
        }

        false
      }
    }


    fun bind() {
      updateViews()
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


  }
}

class MachineDiffUpdateCallBack(private val adapter: MachineJobsAdapter) : ListUpdateCallback {

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
      if(position>0 && (position+count)==it.size){
        adapter.notifyItemChanged(position-1)
      }
    }

    adapter.notifyItemRangeRemoved(position, count)
  }

}

interface MachineJobsAdapterListener {
  fun onJobClicked(job: PrintJob)
  fun onJobLongClicked(position:Int,view:View,job:PrintJob)
  fun onJobMoved(movedJob:PrintJob)
}
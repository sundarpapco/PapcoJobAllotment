package com.papco.sundar.papcojoballotment.common

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

class ItemTouchHelperCallBack(private val callBack:DragCallBack): ItemTouchHelper.Callback() {

  private var dragFrom = -1
  private var dragTo = -1


  override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
    val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
    val swipeFlags = ItemTouchHelper.END
    return makeMovementFlags(dragFlags, swipeFlags)
  }

  override fun onMove( recyclerView: RecyclerView,viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {

    if (dragFrom == -1)
      dragFrom = viewHolder.adapterPosition

    dragTo = target.adapterPosition

    callBack.onDragging(viewHolder.adapterPosition, target.adapterPosition)

    return true
  }

  override fun onSwiped(viewHolder: RecyclerView.ViewHolder, i: Int) {
  }

  override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
    super.onSelectedChanged(viewHolder, actionState)

    //if(actionState==ItemTouchHelper.ACTION_STATE_DRAG){
    if (viewHolder != null)
      viewHolder.itemView.isActivated = true
    //}
  }

  override fun clearView(recyclerView: RecyclerView,viewHolder: RecyclerView.ViewHolder) {
    super.clearView(recyclerView, viewHolder)

    viewHolder.itemView.isActivated = false

    if (dragFrom != -1 && dragTo != -1 && dragFrom != dragTo)
      callBack.onMoved(dragFrom, dragTo)

    dragTo = -1
    dragFrom = dragTo
  }

  override fun isLongPressDragEnabled(): Boolean {
    return false
  }

  override fun isItemViewSwipeEnabled(): Boolean {
    return false
  }

  interface DragCallBack {

    fun onDragging(fromPosition: Int, toPosition: Int)
    fun onMoved(fromPosition: Int, toPosition: Int)
  }
}
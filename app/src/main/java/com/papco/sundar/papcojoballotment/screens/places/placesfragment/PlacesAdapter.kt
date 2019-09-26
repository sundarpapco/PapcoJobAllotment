package com.papco.sundar.papcojoballotment.screens.places.placesfragment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.papco.sundar.papcojoballotment.R
import com.papco.sundar.papcojoballotment.documents.Place
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class PlacesAdapter(val activity: FragmentActivity,val callback: PlacesAdapterListener) :
    RecyclerView.Adapter<PlacesAdapter.PlacesViewHolder>() {

    private var data: List<Place> = LinkedList<Place>()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlacesViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return PlacesViewHolder(inflater.inflate(R.layout.list_item_machine, parent, false))
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: PlacesViewHolder, position: Int) {
        holder.bind()
    }

    fun changeData(newData: List<Place>) {
        refreshData(data, newData)
    }

    private fun refreshData(oldData: List<Place>, newData: List<Place>) {

        activity.lifecycleScope.launch(Dispatchers.Default) {

            val diffResult = DiffUtil.calculateDiff(
                DiffCallBack(oldData, newData)
            )
            if (isActive) {
                withContext(Dispatchers.Main) {
                    data = newData.toMutableList()
                    diffResult.dispatchUpdatesTo(this@PlacesAdapter)
                }
            }

        }

    }


    inner class PlacesViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private var machineName: TextView = view.findViewById(R.id.machine_name)
        private var jobDetails: TextView = view.findViewById(R.id.machine_detail)

        init {
            view.setOnClickListener{
                callback.onPlaceClicked(data[adapterPosition])
            }

            view.setOnLongClickListener{
                callback.onPlaceLongClicked(it,data[adapterPosition])
                true
            }
        }

        fun bind() {
            machineName.text = data[adapterPosition].name
            jobDetails.text = data[adapterPosition].jobDetails()
        }
    }

    inner class DiffCallBack(var oldData: List<Place>, var newData: List<Place>) :
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
            return oldData[oldItemPosition].equals(newData[newItemPosition])
        }

    }

    interface PlacesAdapterListener{
        fun onPlaceClicked(place:Place)
        fun onPlaceLongClicked(view:View,place:Place)
    }
}
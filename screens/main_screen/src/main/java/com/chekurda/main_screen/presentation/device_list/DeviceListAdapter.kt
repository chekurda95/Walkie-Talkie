package com.chekurda.main_screen.presentation.device_list

import android.util.Log
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.chekurda.main_screen.presentation.device_list.DeviceView.DeviceData

internal class DeviceListAdapter(
    private val holderActionListener: DeviceViewHolder.ActionListener
) : RecyclerView.Adapter<DeviceViewHolder>() {

    private var deviceList: List<DeviceData> = emptyList()

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        recyclerView.recycledViewPool.setMaxRecycledViews(DEVICE_VIEW_HOLDER_TYPE, 200)
    }

    fun setDataList(dataList: List<DeviceData>) {
        Log.e("TAGTAG", "setDataList $dataList")
        val diffResult = DiffUtil.calculateDiff(DeviceListDiffUtilCallback(deviceList, dataList))
        deviceList = dataList
        diffResult.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder =
        when (viewType) {
            DEVICE_VIEW_HOLDER_TYPE -> DeviceViewHolder(parent.context, holderActionListener)
            else -> throw IllegalArgumentException("Unsupported view holder type $viewType")
        }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        holder.bind(deviceList[position])
    }

    override fun getItemCount(): Int = deviceList.size
    override fun getItemViewType(position: Int): Int = DEVICE_VIEW_HOLDER_TYPE
}

private class DeviceListDiffUtilCallback(
    private val old: List<DeviceData>,
    private val new: List<DeviceData>
) : DiffUtil.Callback() {

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
        old[oldItemPosition].address == new[newItemPosition].address

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
        old[oldItemPosition] == new[newItemPosition]

    override fun getOldListSize(): Int = old.size
    override fun getNewListSize(): Int = new.size
}

private const val DEVICE_VIEW_HOLDER_TYPE = 1
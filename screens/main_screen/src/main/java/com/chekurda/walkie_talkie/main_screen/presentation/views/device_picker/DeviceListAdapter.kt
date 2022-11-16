package com.chekurda.walkie_talkie.main_screen.presentation.views.device_picker

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.chekurda.common.base_list.calculateDiff
import com.chekurda.walkie_talkie.main_screen.data.DeviceInfo
import com.chekurda.walkie_talkie.main_screen.presentation.views.device_picker.holder.DeviceViewHolder

internal class DeviceListAdapter(
    private val holderActionListener: DeviceViewHolder.ActionListener
) : RecyclerView.Adapter<DeviceViewHolder>() {

    private var deviceList: List<DeviceInfo> = emptyList()

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        recyclerView.recycledViewPool.setMaxRecycledViews(DEVICE_VIEW_HOLDER_TYPE, 200)
    }

    fun setDataList(dataList: List<DeviceInfo>) {
        val diffResult = calculateDiff(deviceList, dataList)
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

private const val DEVICE_VIEW_HOLDER_TYPE = 1
package com.chekurda.walkie_talkie.main_screen.presentation.views.device_picker

import android.content.Context
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.FrameLayout.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout.LayoutParams.WRAP_CONTENT
import android.widget.ProgressBar
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.RecyclerView
import com.chekurda.common.base_list.calculateDiff
import com.chekurda.design.custom_view_tools.utils.dp
import com.chekurda.walkie_talkie.main_screen.data.DeviceInfo
import com.chekurda.walkie_talkie.main_screen.presentation.views.device_picker.holder.DeviceViewHolder

/**
 * Адаптер списка выбора девайсов.
 */
internal class DeviceListAdapter(
    private val itemActionListener: DeviceViewHolder.ActionListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var isSearch: Boolean = false

    private val progressItemIndex: Int
        get() = deviceList.lastIndex + 1

    var deviceList: List<DeviceInfo> = emptyList()
        private set

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        recyclerView.recycledViewPool.setMaxRecycledViews(DEVICE_VIEW_HOLDER_TYPE, 50)
    }

    fun setDataList(dataList: List<DeviceInfo>) {
        val diffResult = calculateDiff(deviceList, dataList)
        deviceList = dataList
        diffResult.dispatchUpdatesTo(this)
    }

    fun changeSearchState(isRunning: Boolean) {
        val isChanged = isSearch != isRunning
        isSearch = isRunning
        if (isChanged && deviceList.isNotEmpty()) {
            if (isRunning) {
                notifyItemInserted(progressItemIndex)
            } else {
                notifyItemRemoved(progressItemIndex)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        when (viewType) {
            DEVICE_VIEW_HOLDER_TYPE -> DeviceViewHolder(parent.context, itemActionListener)
            PROGRESS_VIEW_HOLDER_TYPE -> object : RecyclerView.ViewHolder(ProgressItemView(parent.context)) {}
            else -> throw IllegalArgumentException("Unsupported view holder type $viewType")
        }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DeviceViewHolder) holder.bind(deviceList[position])
    }

    override fun getItemCount(): Int = deviceList.size + if (isSearch && deviceList.isNotEmpty()) 1 else 0
    override fun getItemViewType(position: Int): Int = when {
        position <= deviceList.lastIndex -> DEVICE_VIEW_HOLDER_TYPE
        position == progressItemIndex -> PROGRESS_VIEW_HOLDER_TYPE
        else -> -1
    }
}

private class ProgressItemView(context: Context): FrameLayout(context) {

    init {
        layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
        addView(ProgressBar(context), LayoutParams(dp(30), dp(30), Gravity.CENTER))
        updatePadding(top = dp(10), bottom = dp(10))
    }
}

private const val DEVICE_VIEW_HOLDER_TYPE = 1
private const val PROGRESS_VIEW_HOLDER_TYPE = 2
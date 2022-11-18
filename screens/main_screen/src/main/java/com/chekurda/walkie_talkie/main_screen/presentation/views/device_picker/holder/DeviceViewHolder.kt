package com.chekurda.walkie_talkie.main_screen.presentation.views.device_picker.holder

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import com.chekurda.walkie_talkie.main_screen.data.DeviceInfo

/**
 * ViewHolder списка девайсов.
 */
internal class DeviceViewHolder private constructor(
    private val view: DeviceItemView,
    private val actionListener: ActionListener
): RecyclerView.ViewHolder(view) {

    constructor(
        context: Context,
        actionListener: ActionListener
    ) : this(DeviceItemView(context), actionListener)

    private lateinit var data: DeviceInfo

    init {
        view.setOnClickListener { actionListener.onDeviceItemClicked(data) }
    }

    fun bind(data: DeviceInfo) {
        this.data = data
        view.setData(data)
    }

    fun interface ActionListener {

        fun onDeviceItemClicked(deviceInfo: DeviceInfo)
    }
}
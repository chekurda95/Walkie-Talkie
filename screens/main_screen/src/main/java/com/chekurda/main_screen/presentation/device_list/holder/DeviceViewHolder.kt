package com.chekurda.main_screen.presentation.device_list.holder

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import com.chekurda.main_screen.data.DeviceInfo

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
        view.setOnClickListener { actionListener.onClick(data) }
    }

    fun bind(data: DeviceInfo) {
        this.data = data
        view.setData(data)
    }

    fun interface ActionListener {

        fun onClick(data: DeviceInfo)
    }
}
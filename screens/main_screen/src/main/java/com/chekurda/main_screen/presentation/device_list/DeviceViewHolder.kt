package com.chekurda.main_screen.presentation.device_list

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.chekurda.common.half
import com.chekurda.design.custom_view_tools.TextLayout
import com.chekurda.design.custom_view_tools.utils.MeasureSpecUtils.measureDirection
import com.chekurda.design.custom_view_tools.utils.SimpleTextPaint
import com.chekurda.design.custom_view_tools.utils.dp
import com.chekurda.design.custom_view_tools.utils.safeRequestLayout
import com.chekurda.main_screen.presentation.device_list.DeviceView.DeviceData

internal class DeviceViewHolder private constructor(
    private val view: DeviceView,
    private val actionListener: ActionListener
): RecyclerView.ViewHolder(view) {

    constructor(
        context: Context,
        actionListener: ActionListener
    ) : this(DeviceView(context), actionListener)

    private lateinit var data: DeviceData

    init {
        view.setOnClickListener { actionListener.onClick(data) }
    }

    fun bind(data: DeviceData) {
        this.data = data
        view.setData(data)
    }

    fun interface ActionListener {

        fun onClick(data: DeviceData)
    }
}

internal class DeviceView(context: Context) : View(context) {

    data class DeviceData(
        val address: String,
        val name: String
    )

    private val deviceNameLayout = TextLayout {
        paint = SimpleTextPaint {
            color = Color.BLACK
            textSize = dp(17).toFloat()
        }
    }

    fun setData(data: DeviceData) {
        val isChanged = deviceNameLayout.configure { text = data.name }
        if (isChanged) safeRequestLayout()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(
            measureDirection(widthMeasureSpec) { suggestedMinimumWidth },
            measureDirection(heightMeasureSpec) { suggestedMinimumHeight }
        )
    }

    override fun getSuggestedMinimumWidth(): Int =
        super.getSuggestedMinimumWidth().coerceAtLeast(deviceNameLayout.width)

    override fun getSuggestedMinimumHeight(): Int =
        super.getSuggestedMinimumHeight().coerceAtLeast(dp(40))

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        deviceNameLayout.layout(
            paddingStart,
            paddingTop + (measuredHeight - paddingTop - paddingBottom - deviceNameLayout.height).half
        )
    }

    override fun onDraw(canvas: Canvas) {
        deviceNameLayout.draw(canvas)
    }
}
package com.chekurda.walkie_talkie.main_screen.presentation.device_list.holder

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.view.View
import com.chekurda.common.half
import com.chekurda.design.custom_view_tools.TextLayout
import com.chekurda.design.custom_view_tools.utils.MeasureSpecUtils.measureDirection
import com.chekurda.design.custom_view_tools.utils.SimpleTextPaint
import com.chekurda.design.custom_view_tools.utils.dp
import com.chekurda.design.custom_view_tools.utils.safeRequestLayout
import com.chekurda.walkie_talkie.main_screen.data.DeviceInfo

internal class DeviceItemView(context: Context) : View(context) {

    private val deviceNameLayout = TextLayout {
        paint = SimpleTextPaint {
            color = Color.BLACK
            textSize = dp(TEXT_SIZE_DP).toFloat()
        }
    }

    fun setData(data: DeviceInfo) {
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
        super.getSuggestedMinimumHeight().coerceAtLeast(dp(VIEW_HEIGHT_DP))

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

private const val VIEW_HEIGHT_DP = 40
private const val TEXT_SIZE_DP = 17
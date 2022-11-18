package com.chekurda.walkie_talkie.main_screen.presentation.views.device_picker.holder

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.core.view.updatePadding
import com.chekurda.common.half
import com.chekurda.design.custom_view_tools.TextLayout
import com.chekurda.design.custom_view_tools.utils.MeasureSpecUtils.measureDirection
import com.chekurda.design.custom_view_tools.utils.PAINT_MAX_ALPHA
import com.chekurda.design.custom_view_tools.utils.SimpleTextPaint
import com.chekurda.design.custom_view_tools.utils.dp
import com.chekurda.design.custom_view_tools.utils.safeRequestLayout
import com.chekurda.walkie_talkie.main_screen.data.DeviceInfo
import kotlin.math.roundToInt

internal class DeviceItemView(context: Context) : View(context) {

    private val deviceNameLayout = TextLayout {
        paint = SimpleTextPaint {
            color = Color.BLACK
            textSize = dp(TEXT_SIZE_DP).toFloat()
        }
    }

    private val dividerPaint = SimpleTextPaint {
        color = Color.BLACK
        alpha = (PAINT_MAX_ALPHA * 0.15).roundToInt()
        strokeWidth = dp(1).toFloat()
    }

    init {
        layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
        updatePadding(left = dp(5), right = dp(5), top = dp(12), bottom = dp(12))
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
        super.getSuggestedMinimumHeight().coerceAtLeast(deviceNameLayout.height + paddingTop + paddingBottom)

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        deviceNameLayout.configure { maxWidth = w - paddingStart - paddingEnd }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        deviceNameLayout.layout(
            paddingStart + (measuredWidth - paddingStart - paddingEnd - deviceNameLayout.width).half,
            paddingTop + (measuredHeight - paddingTop - paddingBottom - deviceNameLayout.height).half
        )
    }

    override fun onDraw(canvas: Canvas) {
        deviceNameLayout.draw(canvas)
        canvas.drawLine(0f, height.toFloat(), width.toFloat(), height.toFloat(), dividerPaint)
    }
}

private const val TEXT_SIZE_DP = 24
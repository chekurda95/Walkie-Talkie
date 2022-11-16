package com.chekurda.walkie_talkie.main_screen.presentation.views.device_picker

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Outline
import android.util.AttributeSet
import android.view.View
import android.view.View.MeasureSpec.makeMeasureSpec
import android.view.ViewOutlineProvider
import android.view.ViewPropertyAnimator
import android.widget.FrameLayout
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chekurda.common.half
import com.chekurda.design.custom_view_tools.utils.MeasureSpecUtils.measureDirection
import com.chekurda.design.custom_view_tools.utils.dp
import com.chekurda.design.custom_view_tools.utils.layout
import com.chekurda.walkie_talkie.main_screen.data.DeviceInfo
import com.chekurda.walkie_talkie.main_screen.presentation.views.drawables.BlurBehindDrawable

internal class DevicePickerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private val adapter = DeviceListAdapter { }
    private val recyclerView = RecyclerView(context).apply {
        layoutManager = LinearLayoutManager(context)
        adapter = this@DevicePickerView.adapter
        setBackgroundColor(Color.WHITE)
        outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                outline.setRoundRect(0, 0, view.width, view.height, dp(12).toFloat())
            }
        }
        clipToOutline = true
        clipToPadding = false
        updatePadding(dp(12))
    }
    private val blurDrawable = BlurBehindDrawable(this) { parent as View }.apply {
        setAnimateAlpha(false)
    }

    private var alphaAnimator: ViewPropertyAnimator? = null

    init {
        setWillNotDraw(false)
        addView(recyclerView)
        updatePadding(left = dp(25), right = dp(25))
        alpha = 0f
    }

    fun show() {
        alphaAnimator?.cancel()
        isVisible = true

        alphaAnimator = animate().alpha(1f)
            .setDuration(250)
            .withEndAction { alphaAnimator = null }
            .apply { start() }
        blurDrawable.show(true)
        blurDrawable.invalidate()
    }

    fun hide() {
        alphaAnimator?.cancel()
        isVisible = false

        alphaAnimator = animate().alpha(0f)
            .setDuration(250)
            .withEndAction {
                isVisible = false
                blurDrawable.clear()
                alphaAnimator = null
            }
            .apply { start() }
    }

    fun updateDeviceList(deviceInfoList: List<DeviceInfo>) {
        adapter.setDataList(deviceInfoList)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = measureDirection(widthMeasureSpec) { suggestedMinimumWidth }
        val height = measureDirection(heightMeasureSpec) { suggestedMinimumHeight }
        recyclerView.measure(
            makeMeasureSpec(width - paddingStart - paddingEnd, MeasureSpec.EXACTLY),
            makeMeasureSpec(MeasureSpec.getSize(heightMeasureSpec) / 2, MeasureSpec.EXACTLY)
        )
        setMeasuredDimension(width, height)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        blurDrawable.checkSizes()
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        recyclerView.layout(
            paddingStart,
            paddingTop + (measuredHeight - paddingTop - paddingBottom - recyclerView.measuredHeight).half
        )
    }

    override fun onDraw(canvas: Canvas) {
        blurDrawable.draw(canvas)
        invalidate()
    }
}
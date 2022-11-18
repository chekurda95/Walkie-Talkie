package com.chekurda.walkie_talkie.main_screen.presentation.views.device_picker

import android.content.Context
import android.graphics.Canvas
import android.graphics.Outline
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.View.MeasureSpec.makeMeasureSpec
import android.view.ViewOutlineProvider
import android.view.ViewPropertyAnimator
import android.widget.FrameLayout
import android.widget.ProgressBar
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chekurda.common.half
import com.chekurda.design.custom_view_tools.utils.MeasureSpecUtils.makeExactlySpec
import com.chekurda.design.custom_view_tools.utils.MeasureSpecUtils.measureDirection
import com.chekurda.design.custom_view_tools.utils.dp
import com.chekurda.design.custom_view_tools.utils.layout
import com.chekurda.walkie_talkie.main_screen.R
import com.chekurda.walkie_talkie.main_screen.data.DeviceInfo
import com.chekurda.walkie_talkie.main_screen.presentation.views.device_picker.holder.DeviceViewHolder
import com.chekurda.walkie_talkie.main_screen.presentation.views.drawables.BlurBehindDrawable

internal class DevicePickerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private val blurDrawable = BlurBehindDrawable(this) { parent as View }.apply {
        setAnimateAlpha(false)
    }

    private var alphaAnimator: ViewPropertyAnimator? = null

    private val adapter = DeviceListAdapter { deviceInfo ->
        hide()
        itemActionListener?.onDeviceItemClicked(deviceInfo)
    }
    private val recyclerView = RecyclerView(context).apply {
        layoutManager = object : LinearLayoutManager(context) {
            override fun onLayoutChildren(recycler: RecyclerView.Recycler, state: RecyclerView.State) {
                kotlin.runCatching { super.onLayoutChildren(recycler, state) }
            }
        }
        adapter = this@DevicePickerView.adapter
        background = ContextCompat.getDrawable(context, R.drawable.device_picker_background)!!
        outlineProvider = ViewOutlineProvider.BACKGROUND
        clipToOutline = true
        clipToPadding = false
    }

    private val progressView = ProgressBar(context)
    private val progressSize = dp(40)
    private var isSearchRunning: Boolean = false

    var itemActionListener: DeviceViewHolder.ActionListener? = null

    init {
        setWillNotDraw(false)
        addView(recyclerView)
        addView(progressView)
        updatePadding(left = dp(25), right = dp(25))
    }

    fun show(): Boolean {
        if (isVisible) return false
        alphaAnimator?.cancel()
        isVisible = true
        alpha = 0f
        alphaAnimator = animate().alpha(1f)
            .setDuration(250)
            .withEndAction {
                alpha = 1f
                alphaAnimator = null
            }.apply { start() }
        blurDrawable.show(true)
        blurDrawable.invalidate()
        return true
    }

    fun hide(): Boolean {
        if (!isVisible) return false
        alphaAnimator?.cancel()
        alphaAnimator = animate().alpha(0f)
            .setDuration(250)
            .withEndAction {
                alpha = 0f
                isVisible = false
                blurDrawable.clear()
                alphaAnimator = null
            }
            .apply { start() }
        return true
    }

    fun updateSearchState(isRunning: Boolean) {
        isSearchRunning = isRunning
        progressView.isVisible = isSearchRunning && adapter.deviceList.isEmpty()
        adapter.changeSearchState(isRunning)
        Log.e("TAGTAG", "DevicePicker changeSearchState ${progressView.isVisible}, isRunning = $isRunning, deviceListSize = ${adapter.deviceList.size}")
    }

    fun updateDeviceList(deviceInfoList: List<DeviceInfo>) {
        Log.e("TAGTAG", "DevicePicker updateDeviceList ${deviceInfoList.size}")
        adapter.setDataList(deviceInfoList)
        updateSearchState(isRunning = isSearchRunning)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = measureDirection(widthMeasureSpec) { suggestedMinimumWidth }
        val height = measureDirection(heightMeasureSpec) { suggestedMinimumHeight }
        recyclerView.measure(
            makeMeasureSpec(width - paddingStart - paddingEnd, MeasureSpec.EXACTLY),
            makeMeasureSpec(MeasureSpec.getSize(heightMeasureSpec) / 2, MeasureSpec.EXACTLY)
        )
        progressView.measure(makeExactlySpec(progressSize), makeExactlySpec(progressSize))
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
        progressView.layout(
            recyclerView.left + (recyclerView.measuredWidth - progressView.measuredWidth).half,
            recyclerView.top + (recyclerView.measuredHeight - progressView.measuredHeight).half
        )
    }

    override fun onDraw(canvas: Canvas) {
        blurDrawable.draw(canvas)
        invalidate()
    }
}
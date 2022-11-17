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
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ProgressBar
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chekurda.common.half
import com.chekurda.design.custom_view_tools.utils.MeasureSpecUtils.makeExactlySpec
import com.chekurda.design.custom_view_tools.utils.MeasureSpecUtils.makeUnspecifiedSpec
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

    private val searchButton = Button(context).apply {
        text = "Continue search"
        setTextAppearance(R.style.search_button_text_style)
        updatePadding(
            left = dp(25),
            right = dp(25),
            top = dp(10),
            bottom = dp(10)
        )
        outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                outline.setRoundRect(0, 0, view.width, view.height, dp(25).toFloat())
            }
        }
        clipToOutline = true
        setBackgroundResource(R.color.search_button_background_color)
        setOnClickListener {
            searchButtonClickListener?.invoke()
        }
    }

    private val progressView = ProgressBar(context)
    private val progressSize = dp(30)

    var itemActionListener: DeviceViewHolder.ActionListener? = null
    var searchButtonClickListener: SearchButtonClickListener? = null

    init {
        setWillNotDraw(false)
        addView(recyclerView)
        addView(searchButton)
        addView(progressView)
        updatePadding(left = dp(25), right = dp(25))
    }

    fun show() {
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
    }

    fun hide() {
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
    }

    fun changeSearchState(isRunning: Boolean) {
        searchButton.isVisible = !isRunning
        progressView.isVisible = isRunning && adapter.deviceList.isEmpty()
        adapter.changeSearchState(isRunning)
    }

    fun updateDeviceList(deviceInfoList: List<DeviceInfo>) {
        if (!isVisible) return
        adapter.setDataList(deviceInfoList)
        if (progressView.isVisible && deviceInfoList.isNotEmpty()) {
            progressView.isVisible = false
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = measureDirection(widthMeasureSpec) { suggestedMinimumWidth }
        val height = measureDirection(heightMeasureSpec) { suggestedMinimumHeight }
        recyclerView.measure(
            makeMeasureSpec(width - paddingStart - paddingEnd, MeasureSpec.EXACTLY),
            makeMeasureSpec(MeasureSpec.getSize(heightMeasureSpec) / 2, MeasureSpec.EXACTLY)
        )
        searchButton.measure(makeUnspecifiedSpec(), makeUnspecifiedSpec())
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
        searchButton.layout(
            paddingStart + (measuredWidth - paddingStart - paddingEnd - searchButton.measuredWidth).half,
            recyclerView.bottom + (measuredHeight - paddingBottom - recyclerView.bottom - searchButton.measuredHeight).half
        )
    }

    override fun onDraw(canvas: Canvas) {
        blurDrawable.draw(canvas)
        invalidate()
    }
}

internal typealias SearchButtonClickListener = () -> Unit
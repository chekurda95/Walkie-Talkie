package com.chekurda.walkie_talkie.main_screen.presentation.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import androidx.annotation.FloatRange
import androidx.core.view.updatePadding
import com.chekurda.common.half
import com.chekurda.design.custom_view_tools.TextLayout
import com.chekurda.design.custom_view_tools.utils.MeasureSpecUtils.measureDirection
import com.chekurda.design.custom_view_tools.utils.dp
import com.chekurda.design.custom_view_tools.utils.safeRequestLayout
import com.chekurda.walkie_talkie.main_screen.presentation.views.drawables.RuntimeWaveformDrawable
import org.apache.commons.lang3.StringUtils

internal class ConnectionInfoView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    data class ConnectionInfo(
        val deviceName: String = StringUtils.SPACE,
        val isConnected: Boolean = false
    )

    private val waveformHeight = dp(WAVEFORM_HEIGHT_DP)
    private val connectedText = "Connected"
    private val notConnectedText = "No connection"

    private val deviceNameLayout = TextLayout {
        paint.apply {
            color = Color.WHITE
            typeface = Typeface.DEFAULT_BOLD
            textSize = dp(TEXT_SIZE_DP).toFloat()
        }
        text = StringUtils.SPACE
    }
    private val connectionStateLayout = TextLayout {
        paint.apply {
            color = Color.WHITE
            typeface = Typeface.DEFAULT_BOLD
            textSize = dp(TEXT_SIZE_DP).toFloat()
        }
        text = notConnectedText
    }
    private val waveformDrawable = RuntimeWaveformDrawable(this).apply {
        callback = this@ConnectionInfoView
    }

    var connectionData: ConnectionInfo = ConnectionInfo()
        set(value) {
            val isChanged = field != value
            field = value
            if (isChanged) {
                connectionStateLayout.configure { text = if (value.isConnected) connectedText else notConnectedText }
                deviceNameLayout.configure { text = value.deviceName }
                safeRequestLayout()
            }
        }

    @get:FloatRange(from = 0.0, to = 1.0)
    var inputAmplitude: Float
        get() = waveformDrawable.amplitude
        set(value) {
            waveformDrawable.amplitude = value
        }

    init {
        updatePadding(left = dp(25), right = dp(25))
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(
            measureDirection(widthMeasureSpec) { suggestedMinimumWidth },
            measureDirection(heightMeasureSpec) { suggestedMinimumHeight }
        )
    }

    override fun getSuggestedMinimumWidth(): Int =
        super.getSuggestedMinimumWidth()
            .coerceAtLeast(deviceNameLayout.width + paddingStart + paddingEnd)

    override fun getSuggestedMinimumHeight(): Int =
        super.getSuggestedMinimumHeight()
            .coerceAtLeast(
                deviceNameLayout.height.plus(connectionStateLayout.height)
                    .plus(waveformHeight)
                    .plus(paddingTop + paddingBottom)
            )

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        deviceNameLayout.configure { maxWidth = w - paddingStart - paddingEnd }
        connectionStateLayout.configure { maxWidth = w - paddingStart - paddingEnd }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        val availableHeight = measuredHeight - paddingTop - paddingBottom
        val waveformTop = paddingTop + (availableHeight - waveformHeight).half
        waveformDrawable.setBounds(
            paddingStart,
            waveformTop,
            measuredWidth - paddingEnd,
            waveformTop + waveformHeight
        )

        deviceNameLayout.layout(
            paddingStart + (measuredWidth - paddingStart - paddingEnd - deviceNameLayout.width).half,
            paddingTop + (waveformDrawable.bounds.top - paddingTop - deviceNameLayout.height).half
        )
        connectionStateLayout.layout(
            paddingStart + (measuredWidth - paddingStart - paddingEnd - connectionStateLayout.width).half,
            waveformDrawable.bounds.bottom + (measuredHeight - paddingBottom - waveformDrawable.bounds.bottom - connectionStateLayout.height).half
        )
    }

    override fun onDraw(canvas: Canvas) {
        deviceNameLayout.draw(canvas)
        connectionStateLayout.draw(canvas)
        waveformDrawable.draw(canvas)
    }

    override fun verifyDrawable(who: Drawable): Boolean =
        who == waveformDrawable || super.verifyDrawable(who)
}

private const val TEXT_SIZE_DP = 20
private const val WAVEFORM_HEIGHT_DP = 60
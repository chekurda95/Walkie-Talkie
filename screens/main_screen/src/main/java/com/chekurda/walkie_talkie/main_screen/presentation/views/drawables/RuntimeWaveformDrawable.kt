package com.chekurda.walkie_talkie.main_screen.presentation.views.drawables

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.view.View
import androidx.annotation.FloatRange
import androidx.core.graphics.withTranslation
import com.chekurda.design.custom_view_tools.utils.SimplePaint
import com.chekurda.design.custom_view_tools.utils.update
import java.util.LinkedList
import kotlin.math.roundToInt

/**
 * Осциллограмма входящего звука.
 */
internal class RuntimeWaveformDrawable(private val view: View) : Drawable() {

    private val paint = SimplePaint {
        color = Color.WHITE
        style = Paint.Style.FILL
    }

    @get:FloatRange(from = 0.0, to = 1.0)
    var amplitude: Float = MIN_AMPLITUDE
        set(value) {
            val rangedValue = value.coerceAtMost(1f).coerceAtLeast(MIN_AMPLITUDE)
            if (field == rangedValue) return
            field = rangedValue
            isRunning = true
            invalidateSelf()
        }

    private var amplitudes: LinkedList<Rect> = LinkedList()
    private var rectWidth = 0
    private var isRunning = false
    private var minHeight = 0
    private var skipFrame = false

    override fun getIntrinsicWidth(): Int = bounds.width()
    override fun getIntrinsicHeight(): Int = bounds.height()

    override fun setBounds(left: Int, top: Int, right: Int, bottom: Int) {
        super.setBounds(left, top, right, bottom)
        rectWidth = bounds.width() / (COLUMNS_COUNT * 2 - 1)
        minHeight = (MIN_AMPLITUDE * bounds.height()).roundToInt()
        if (amplitudes.isEmpty()) repeat(COLUMNS_COUNT) {
            amplitudes.add(Rect().update(top = bounds.bottom - minHeight, bottom = bounds.bottom, right = rectWidth))
        }
    }

    override fun draw(canvas: Canvas) {
        if (isRunning && !skipFrame) {
            amplitudes.removeFirst().apply {
                update(top = bounds.bottom - (bounds.height() * amplitude).roundToInt())
                amplitudes.addLast(this)
            }
        }
        var right = bounds.left - rectWidth.toFloat()
        isRunning = false
        amplitudes.forEach {
            if (!isRunning) isRunning = it.height() > minHeight
            right += rectWidth.toFloat()
            canvas.withTranslation(x = right + rectWidth.toFloat()) {
                canvas.drawRect(it, paint)
            }
            right += rectWidth.toFloat()
        }
        if (isRunning) {
            skipFrame = !skipFrame
            invalidateSelf()
        }
    }

    override fun setAlpha(alpha: Int) = Unit

    override fun setColorFilter(colorFilter: ColorFilter?) = Unit

    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT
}

private const val COLUMNS_COUNT = 15
private const val MIN_AMPLITUDE = 0.05f
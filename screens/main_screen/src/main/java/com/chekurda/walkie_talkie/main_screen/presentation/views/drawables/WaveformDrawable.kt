package com.chekurda.walkie_talkie.main_screen.presentation.views.drawables

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import com.chekurda.design.custom_view_tools.utils.SimplePaint

internal class WaveformDrawable : Drawable() {

    private val paint = SimplePaint {
        color = Color.WHITE
        style = Paint.Style.FILL
    }

    override fun getIntrinsicWidth(): Int = bounds.width()
    override fun getIntrinsicHeight(): Int = bounds.height()

    override fun draw(canvas: Canvas) {
        canvas.drawRect(bounds, paint)
    }

    override fun setAlpha(alpha: Int) = Unit

    override fun setColorFilter(colorFilter: ColorFilter?) = Unit

    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT
}
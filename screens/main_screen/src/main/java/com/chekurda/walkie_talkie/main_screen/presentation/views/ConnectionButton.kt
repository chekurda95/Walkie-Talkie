package com.chekurda.walkie_talkie.main_screen.presentation.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import android.view.ViewOutlineProvider
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.chekurda.common.half
import com.chekurda.design.custom_view_tools.TextLayout
import com.chekurda.design.custom_view_tools.utils.MeasureSpecUtils.measureDirection
import com.chekurda.design.custom_view_tools.utils.dp
import com.chekurda.design.custom_view_tools.utils.safeRequestLayout
import com.chekurda.walkie_talkie.main_screen.R
import com.chekurda.walkie_talkie.main_screen.presentation.views.drawables.AnimatedDotsDrawable

internal class ConnectionButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    enum class ButtonState(val text: String, @DrawableRes val backgroundRes: Int) {
        CONNECT_SUGGESTION("Connect".uppercase(), R.drawable.connect_ripple_button_background),
        DISCONNECT_SUGGESTION("Disconnect".uppercase(), R.drawable.disconnect_ripple_button_background),
        WAITING_CONNECTION("Connecting".uppercase(), R.drawable.connect_ripple_button_background)
    }

    private val textLayout = TextLayout {
        paint.apply {
            color = Color.WHITE
            typeface = Typeface.DEFAULT_BOLD
            textSize = dp(17).toFloat()
        }
    }

    private val dotsDrawable = AnimatedDotsDrawable().apply {
        callback = this@ConnectionButton
        params = AnimatedDotsDrawable.DotsParams(size = dp(3))
        textColor = Color.WHITE
    }
    private val dotsSpacing = dp(2)

    var buttonState: ButtonState = ButtonState.CONNECT_SUGGESTION
        set(value) {
            field = value
            val isChanged = textLayout.configure { text = value.text }
            if (isChanged) {
                background = ContextCompat.getDrawable(context, value.backgroundRes)
                safeRequestLayout()
            }
        }

    init {
        buttonState = ButtonState.CONNECT_SUGGESTION
        outlineProvider = ViewOutlineProvider.BACKGROUND
        clipToOutline = true
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        dotsDrawable.setVisible(buttonState == ButtonState.WAITING_CONNECTION, false)
        setMeasuredDimension(
            measureDirection(widthMeasureSpec) { suggestedMinimumWidth },
            measureDirection(widthMeasureSpec) { suggestedMinimumHeight },
        )
    }

    override fun getSuggestedMinimumWidth(): Int =
        super.getSuggestedMinimumWidth()
            .coerceAtLeast(textLayout.width + paddingStart + paddingEnd + if (dotsDrawable.isVisible) dotsSpacing + dotsDrawable.intrinsicWidth else 0)

    override fun getSuggestedMinimumHeight(): Int =
        super.getSuggestedMinimumHeight().coerceAtLeast(textLayout.height + paddingTop + paddingBottom)

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        if (dotsDrawable.isVisible) {
            textLayout.layout(
                paddingStart + (measuredWidth - paddingStart - paddingEnd - textLayout.width - dotsDrawable.intrinsicWidth).half,
                paddingTop + (measuredHeight - paddingTop - paddingBottom - textLayout.height).half
            )
            val dotsLeft = textLayout.right + dotsSpacing
            val dotsTop = textLayout.top + textLayout.baseline - dotsDrawable.intrinsicHeight
            dotsDrawable.setBounds(
                dotsLeft,
                dotsTop,
                dotsLeft + dotsDrawable.intrinsicWidth,
                dotsTop + dotsDrawable.intrinsicHeight
            )
        } else {
            textLayout.layout(
                paddingStart + (measuredWidth - paddingStart - paddingEnd - textLayout.width).half,
                paddingTop + (measuredHeight - paddingTop - paddingBottom - textLayout.height).half
            )
        }
    }

    override fun onDraw(canvas: Canvas) {
        textLayout.draw(canvas)
        dotsDrawable.draw(canvas)
    }

    override fun verifyDrawable(who: Drawable): Boolean =
        who == dotsDrawable || super.verifyDrawable(who)
}
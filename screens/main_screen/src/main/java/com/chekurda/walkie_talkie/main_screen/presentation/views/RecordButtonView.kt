package com.chekurda.walkie_talkie.main_screen.presentation.views

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.text.Layout
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.annotation.FloatRange
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.updateBounds
import androidx.core.graphics.withScale
import com.chekurda.common.half
import com.chekurda.design.custom_view_tools.TextLayout
import com.chekurda.design.custom_view_tools.utils.MeasureSpecUtils.measureDirection
import com.chekurda.design.custom_view_tools.utils.PAINT_MAX_ALPHA
import com.chekurda.design.custom_view_tools.utils.SimplePaint
import com.chekurda.design.custom_view_tools.utils.dp
import com.chekurda.walkie_talkie.main_screen.R
import com.chekurda.walkie_talkie.main_screen.presentation.views.drawables.AmplitudeDrawable

/**
 * Кнопка записи звука с амплитудой громкости микрофона.
 */
internal class RecordButtonView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val dimens = RecordButtonDimens.create(resources)
    private val rootBackgroundColor = ContextCompat.getColor(context, R.color.record_button_background_color)

    private val textLayout = TextLayout {
        paint.apply {
            typeface = Typeface.DEFAULT_BOLD
            textSize = dp(20).toFloat()
            color = Color.WHITE
        }
        text = "Push".uppercase()
        alignment = Layout.Alignment.ALIGN_CENTER
        includeFontPad = false
    }

    private val buttonBackgroundPaint = SimplePaint {
        color = rootBackgroundColor
        style = Paint.Style.FILL
    }

    private val amplitudeDrawable = AmplitudeDrawable(
        SimplePaint {
            color = rootBackgroundColor
            style = Paint.Style.FILL
            alpha = AMPLITUDE_PAINT_ALPHA
        }
    ).apply {
        callback = this@RecordButtonView
        minRadius = dimens.amplitudeMinSize / 2f
        maxRadius = dimens.amplitudeMaxSize.toFloat() / 2f
        pulsationRadiusDx = dimens.amplitudePulseRadiusDx.toFloat()
    }

    private var center = 0f to 0f
    private var backgroundScale: Float = 0f
    private val scaleOnInterpolator = DecelerateInterpolator()
    private val scaleOffInterpolator = AccelerateInterpolator()
    private var amplitudeAnimator: ValueAnimator? = null

    /**
     * Амплитуда громкости микрофона.
     */
    @get:FloatRange(from = 0.0, to = 1.0)
    var amplitude: Float = 0f
        set(value) {
            amplitudeAnimator?.cancel()
            val rangedValue = value.coerceAtMost(1f).coerceAtLeast(0f)
            if (field == rangedValue) return
            field = rangedValue
            amplitudeDrawable.amplitude = field
        }

    /**
     * Доля анимации появления кнопки записи.
     */
    private var showingFraction: Float = 0f
        set(value) {
            val fraction = minOf(value, 1f)
            if (field == fraction) return
            field = fraction
            val scaleOnEndPoint = 2 / 3f
            val scaleDiff = MAX_BUTTON_SCALE - 1f
            backgroundScale = if (field <= scaleOnEndPoint) {
                val scaleOnFraction = field / scaleOnEndPoint
                val interpolation = scaleOnInterpolator.getInterpolation(scaleOnFraction)
                MAX_BUTTON_SCALE * interpolation
            } else {
                val scaleOfFraction = (field - scaleOnEndPoint) / (1f - scaleOnEndPoint)
                val interpolation = scaleOffInterpolator.getInterpolation(scaleOfFraction)
                MAX_BUTTON_SCALE - scaleDiff * interpolation
            }
            invalidate()
        }

    init {
        showingFraction = 1f
    }

    /**
     * Очистить состояние кнопки записи.
     */
    fun clear() {
        showingFraction = 0f
        amplitude = 0f
        amplitudeDrawable.clear()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(
            measureDirection(widthMeasureSpec) { suggestedMinimumWidth },
            measureDirection(heightMeasureSpec) { suggestedMinimumHeight }
        )
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        textLayout.layout(
            center.first.toInt() - textLayout.width.half,
            center.second.toInt() - textLayout.height.half
        )
    }

    override fun onDraw(canvas: Canvas) {
        if (backgroundScale == 0f) return
        drawAmplitude(canvas)
        drawButtonBackground(canvas)
        textLayout.draw(canvas)
    }

    private fun drawAmplitude(canvas: Canvas) {
        canvas.withScale(backgroundScale, backgroundScale, center.first, center.second) {
            amplitudeDrawable.draw(canvas)
        }
    }

    private fun drawButtonBackground(canvas: Canvas) {
        canvas.drawCircle(
            center.first,
            center.second,
            backgroundScale * dimens.buttonRadius + amplitudeDrawable.animatedAmplitude * dimens.maxButtonRadiusDelta,
            buttonBackgroundPaint
        )
    }

    override fun getSuggestedMinimumWidth(): Int =
        paddingStart + paddingEnd + dimens.amplitudeMaxSize

    override fun getSuggestedMinimumHeight(): Int =
        paddingTop + paddingBottom + dimens.amplitudeMaxSize

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        val availableWidth = w - paddingStart - paddingEnd
        val availableHeight = h - paddingTop - paddingBottom
        val centerX = availableWidth / 2f
        val centerY = availableHeight / 2f
        center = paddingStart + centerX to paddingTop + centerY
        amplitudeDrawable.updateBounds(
            left = paddingStart,
            top = paddingTop,
            right = measuredWidth - paddingEnd,
            bottom = measuredHeight - paddingBottom
        )
    }

    override fun verifyDrawable(who: Drawable): Boolean =
        who == amplitudeDrawable || super.verifyDrawable(who)

    fun animateAmplitudeCancel() {
        amplitudeAnimator?.cancel()
        val currentAmplitude = amplitude
        ValueAnimator.ofFloat(0f, 1f).apply {
            amplitudeAnimator = this
            duration = 300
            addUpdateListener {
                amplitudeDrawable.amplitude = currentAmplitude * scaleOffInterpolator.getInterpolation(1f - it.animatedFraction)
            }
            doOnEnd { amplitude = 0f }
            start()
        }
    }

    /**
     * Размеры внутренней разметки кнопки записи [RecordButtonView].
     */
    private data class RecordButtonDimens(
        val buttonSize: Int,
        val buttonRadius: Float,
        val maxButtonRadiusDelta: Float,
        val amplitudeMinSize: Int,
        val amplitudeMaxSize: Int,
        val availableAmplitudeRadiusDx: Float,
        val amplitudePulseRadiusDx: Int
    ) {
        companion object {
            fun create(resources: Resources) = with(resources) {
                val buttonSize = dp(120)
                val buttonRadius = buttonSize / 2f
                val amplitudeMinSize = dp(130)
                val amplitudeMaxSize = dp(250)
                RecordButtonDimens(
                    buttonSize = buttonSize,
                    buttonRadius = buttonRadius,
                    maxButtonRadiusDelta = buttonRadius * BACKGROUND_AMPLITUDE_SCALE,
                    amplitudeMinSize = amplitudeMinSize,
                    amplitudeMaxSize = amplitudeMaxSize,
                    availableAmplitudeRadiusDx = (amplitudeMaxSize - amplitudeMinSize) / 2f,
                    amplitudePulseRadiusDx = dp(5)
                )
            }
        }
    }
}

/**
 * Процент прозрачности амплитуды громкости микрофона.
 */
private const val AMPLITUDE_ALPHA_PERCENT = 0.4
/**
 * Прозрачность краски амплитуды громкости микрофона.
 */
private const val AMPLITUDE_PAINT_ALPHA = (PAINT_MAX_ALPHA * AMPLITUDE_ALPHA_PERCENT).toInt()
/**
 * Максимальный масштаб кнопки во время анимации.
 */
private const val MAX_BUTTON_SCALE = 1.1f

private const val BACKGROUND_AMPLITUDE_SCALE = 0.50f
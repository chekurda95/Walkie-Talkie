package com.chekurda.walkie_talkie.main_screen.presentation.views.drawables

import android.animation.ValueAnimator
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import androidx.annotation.ColorInt
import androidx.annotation.FloatRange
import androidx.annotation.Px
import kotlin.math.abs

/**
 * Drawable амплитуды громкости микрофона при записи аудио.
 */
internal class AmplitudeDrawable(private val paint: Paint) : Drawable() {

    private var amplitudeScale = 0f
    private var amplitudeChangingSpeed = 0f
    private var lastDrawingTimeMs = System.currentTimeMillis()

    var minRadius = 0f
        set(value) {
            if (field == value) return
            field = value
        }

    var maxRadius = 0f
        set(value) {
            field = value
            amplitudeScale = maxRadius / minRadius - 1f
        }

    @get:Px
    var pulsationRadiusDx = 0f

    @get:Px
    private val amplitudeAvailableDiff: Float
        get() = maxRadius - minRadius

    @get:FloatRange(from = 0.0, to = 1.0)
    private val pulsationPick: Float
        get() = pulsationRadiusDx / amplitudeAvailableDiff
    private var animatePulsation: Boolean = false
    private var isPulseGrowing = true
    private var pulsationProgress = 0f
    private val pulsationInterpolator = AccelerateDecelerateInterpolator()
    private var pulseStableAmplitude = 0f
    private var isRunning: Boolean = false

    var animatedAmplitude = 0f
        private set

    @FloatRange(from = 0.0, to = 1.0)
    var amplitude: Float = 0f
        set(value) {
            if (field == value) return
            field = value
            val animatedAmplitudeDelta = value - animatedAmplitude
            val animationTime = if (value > animatedAmplitude) EXPAND_ANIMATION_TIME_MS else COLLAPSE_ANIMATION_TIME_MS
            amplitudeChangingSpeed = animatedAmplitudeDelta / animationTime
            checkPulsationAnimation(value)
            isRunning = true
            invalidateSelf()
        }

    fun clear() {
        amplitude = 0f
        animatedAmplitude = 0f
        amplitudeChangingSpeed = 0f
        isPulseGrowing = true
        animatePulsation = false
        isRunning = false
        setVisible(true, false)
    }

    fun setColor(@ColorInt color: Int, alpha: Int) {
        paint.color = color
        paint.alpha = alpha
    }

    override fun draw(canvas: Canvas) {
        if (!isVisible) return
        val currentTime = System.currentTimeMillis()
        val dt = minOf(currentTime - lastDrawingTimeMs, FRAME_TIME_MS)
        lastDrawingTimeMs = currentTime
        updateAmplitude(dt)

        val centerX = bounds.centerX().toFloat()
        val centerY = bounds.centerY().toFloat()
        canvas.drawCircle(centerX, centerY, minRadius + amplitudeAvailableDiff * animatedAmplitude, paint)
        if (isRunning) invalidateSelf()
    }

    private fun checkPulsationAnimation(newAmplitude: Float) {
        if (animatePulsation) {
            val pulseStableRange = (pulseStableAmplitude - pulsationPick)..(pulseStableAmplitude + pulsationPick)
            animatePulsation = newAmplitude in pulseStableRange
        } else {
            val endRange = (newAmplitude - ANIMATION_END_POINT_DELTA)..(newAmplitude + ANIMATION_END_POINT_DELTA)
            val isLowAmplitudeSpeed = abs(amplitudeChangingSpeed) <= PULSE_AVAILABLE_ANIMATION_SPEED
            if (animatedAmplitude in endRange && isLowAmplitudeSpeed) {
                animatePulsation = true
                pulseStableAmplitude = newAmplitude
                pulsationProgress = 0f
                isPulseGrowing = true
            }
        }
    }

    private fun updateAmplitude(dt: Long) {
        if (animatePulsation) {
            updatePulsationAmplitude(dt)
        } else {
            updateAnimatedAmplitude(dt)
        }
    }

    private fun updatePulsationAmplitude(dt: Long) {
        pulsationProgress = minOf(pulsationProgress + dt / PULSATION_TIME_MS, 1f)
        val interpolation = pulsationInterpolator.getInterpolation(pulsationProgress)
        animatedAmplitude = pulseStableAmplitude + if (isPulseGrowing) {
            interpolation * pulsationPick
        } else {
            pulsationPick - interpolation * pulsationPick
        }
        if (pulsationProgress == 1f) {
            pulsationProgress = 0f
            isPulseGrowing = !isPulseGrowing
        }
    }

    private fun updateAnimatedAmplitude(dt: Long) {
        if (amplitude == animatedAmplitude) return
        animatedAmplitude += amplitudeChangingSpeed * dt
        val isSoBig = amplitudeChangingSpeed > 0 && animatedAmplitude > amplitude
        val isSoSmall = amplitudeChangingSpeed < 0 && animatedAmplitude < amplitude
        if (isSoBig || isSoSmall) {
            animatedAmplitude = amplitude
            if (isSoSmall) {
                amplitudeChangingSpeed = 0f
                animatePulsation = true
                pulseStableAmplitude = 0f
                pulsationProgress = 0f
                isPulseGrowing = true
            }
        }
    }

    override fun setAlpha(alpha: Int) = Unit

    override fun setColorFilter(colorFilter: ColorFilter?) = Unit

    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT
}

private const val EXPAND_ANIMATION_TIME_MS = 150f
private const val COLLAPSE_ANIMATION_TIME_MS = 250f
private const val PULSATION_TIME_MS = 1500f
private const val PULSE_AVAILABLE_ANIMATION_SPEED = 0.005f
private const val ANIMATION_END_POINT_DELTA = 0.01f
private const val FRAME_TIME_MS = 17L
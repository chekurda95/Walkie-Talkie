package com.chekurda.walkie_talkie.main_screen.presentation.views.drawables

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import android.view.animation.AccelerateInterpolator
import androidx.annotation.ColorInt
import androidx.annotation.Px

/**
 * Drawable для отображения анимируемых точек.
 * @see DotsParams
 */
internal class AnimatedDotsDrawable : Drawable() {

    /**
     * Параметры анимируемых точек.
     *
     * @property fadeInDurationMs общее время продолжительности анимации появления всех точек в мс.
     * @property fadeOutDurationMs продолжительность анимации затухания точек в мс.
     * @property count количество точек.
     * @property size размер точек в px.
     * @property spacing отстыпы между точками в px.
     */
    data class DotsParams(
        val fadeInDurationMs: Int = DEFAULT_FADE_IN_ANIMATION_DURATION_MS,
        val fadeOutDurationMs: Int = DEFAULT_FADE_OUT_ANIMATION_DURATION_MS,
        val count: Int = DEFAULT_DOTS_COUNT,
        @Px val size: Int = DEFAULT_DOTS_SIZE_PX,
        @Px val spacing: Int = size
    ) {
        val oneStepDurationMs: Int = fadeInDurationMs / count
        val dotRadius: Float = size / 2f
    }

    /**
     * Установить/получить параметры с настройками анимируемых точек.
     * @see DotsParams
     */
    var params = DotsParams()
        set(value) {
            val isChanged = field != value
            field = value

            if (isChanged) {
                clearSteps()
                invalidateSelf()
            }
        }

    @get:ColorInt
    var textColor: Int = Color.GRAY
        set(value) {
            field = value
            paint.color = value
            fadePaint.color = value
        }

    /**
     * Основная краска, которой рисуются отображаемые точки.
     */
    private val paint = Paint().apply {
        isAntiAlias = true
        color = textColor
    }

    /**
     * Вспомогательная краска для отрисовки появления или исчезновения точек.
     */
    private val fadePaint = Paint().apply {
        isAntiAlias = true
        color = textColor
    }

    /**
     * Интерполятор анимации затухания точек.
     */
    private val fadeOutInterpolator = AccelerateInterpolator()

    /**
     * Время обновления шага анимации в мс.
     */
    private var stepUpdateTimeMs = 0L

    /**
     * Номер самого последнего шага анимации.
     */
    private val lastStep: Int
        get() = params.count

    /**
     * Текущий шаг анимации.
     */
    private var step = 0
        set(value) {
            field = value % (lastStep + 1)
        }

    /**
     * Сбросить все шаги анимации к исходному состоянию.
     */
    private fun clearSteps() {
        step = 0
        stepUpdateTimeMs = System.currentTimeMillis()
    }

    override fun setVisible(visible: Boolean, restart: Boolean): Boolean =
        super.setVisible(visible, restart).also {
            if (restart) clearSteps()
        }

    override fun getIntrinsicWidth(): Int =
        with(params) { size * count + spacing * (count - 1) }

    override fun getIntrinsicHeight(): Int =
        params.size

    override fun draw(canvas: Canvas) {
        if (!isVisible) return
        val currentTime = System.currentTimeMillis()

        val interpolationForStep = if (step != lastStep) {
            minOf((currentTime - stepUpdateTimeMs) / params.oneStepDurationMs.toFloat(), 1f)
        } else {
            val interpolation = minOf((currentTime - stepUpdateTimeMs) / params.fadeOutDurationMs.toFloat(), 1f)
            1f - fadeOutInterpolator.getInterpolation(interpolation)
        }
        fadePaint.alpha = (interpolationForStep * paint.alpha).toInt()

        repeat(params.count) { dotIndex ->
            val dotPaint = when {
                // Появление точки или исчезновение всех точек
                dotIndex == step || step == lastStep -> fadePaint
                // Точка просто отображается без анимаций
                dotIndex <= step -> paint
                // Для остальных очередь не дошла - не рисуем
                else -> return@repeat
            }
            val dotHorizontalCenter = bounds.left + params.dotRadius + params.size * dotIndex + params.spacing * dotIndex
            canvas.drawCircle(dotHorizontalCenter, bounds.top + params.dotRadius, params.dotRadius, dotPaint)
        }

        val isFadeInEnd = step != lastStep && currentTime - stepUpdateTimeMs >= params.oneStepDurationMs
        val isFadeOutEnd = step == lastStep && currentTime - stepUpdateTimeMs >= params.fadeOutDurationMs
        if (isFadeInEnd || isFadeOutEnd) {
            step++
            stepUpdateTimeMs = currentTime
        }
        invalidateSelf()
    }

    override fun setAlpha(alpha: Int) {
        paint.alpha = alpha
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        paint.colorFilter = colorFilter
        fadePaint.colorFilter = colorFilter
    }

    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT
}

/**
 * Стандартное количество анимируемых точек.
 */
private const val DEFAULT_DOTS_COUNT = 3

/**
 * Стандартная продолжительность анимации появления всех точек.
 */
private const val DEFAULT_FADE_IN_ANIMATION_DURATION_MS = 500

/**
 * Стандартная продолжительность анимации затухания всех точек.
 */
private const val DEFAULT_FADE_OUT_ANIMATION_DURATION_MS = 500

/**
 * Стандартный размер точек в px.
 */
private const val DEFAULT_DOTS_SIZE_PX = 50
package com.chekurda.design.custom_view_tools.utils

import android.graphics.Rect
import android.graphics.RectF
import com.chekurda.common.half

/**
 * Обновить позиции [Rect]
 */
fun Rect.update(
    left: Int = this.left,
    top: Int = this.top,
    right: Int = this.right,
    bottom: Int = this.bottom
): Rect = apply {
    set(left, top, right, bottom)
}

/**
 * Обновить позиции [RectF]
 */
fun RectF.update(
    left: Float = this.left,
    top: Float = this.top,
    right: Float = this.right,
    bottom: Float = this.bottom
): RectF = apply {
    set(left, top, right, bottom)
}

/**
 * Преобразование [Rect] в [RectF]
 */
fun Rect.toFloat(): RectF =
    RectF(
        left.toFloat(),
        top.toFloat(),
        right.toFloat(),
        bottom.toFloat()
    )

/**
 * Преобразование [RectF] в [Rect]
 */
fun RectF.toInt(): Rect =
    Rect(
        left.toInt(),
        top.toInt(),
        right.toInt(),
        bottom.toInt()
    )

/**
 * Копирование [Rect] в новый объект
 */
fun Rect.copy() = Rect(this)

/**
 * Копирование [RectF] в новый объект
 */
fun RectF.copy() = RectF(this)

/**
 * Масштабирование [RectF] относительно центра
 */
fun RectF.scale(value: Float): RectF {
    val scaledWidthHalf = width().half * value
    val scaledHeightHalf = height().half * value
    val center = centerX() to centerY()
    return update(
        center.first - scaledWidthHalf,
        center.second - scaledHeightHalf,
        center.first + scaledWidthHalf,
        center.second + scaledHeightHalf
    )
}
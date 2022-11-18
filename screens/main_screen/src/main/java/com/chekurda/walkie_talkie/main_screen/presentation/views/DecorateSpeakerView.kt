package com.chekurda.walkie_talkie.main_screen.presentation.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import com.chekurda.common.half
import com.chekurda.design.custom_view_tools.utils.SimplePaint
import com.chekurda.design.custom_view_tools.utils.dp
import kotlin.math.roundToInt

/**
 * Декоративный view элемент для отображения динамика рации.
 */
internal class DecorateSpeakerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val paint = SimplePaint {
        style = Paint.Style.FILL
        color = Color.BLACK
    }

    private val rowsCount = VERTICAL_COUNT
    private val columnsCount = HORIZONTAL_COUNT * 2 - 1
    private val radius = dp(CORNERS_DP).toFloat()

    private var itemWidth = 0
    private var itemHeight = 0
    private val rectList: List<RectF> = arrayListOf<RectF>().apply {
        repeat(rowsCount * columnsCount) { add(RectF()) }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        itemWidth = 2 * ((w - paddingStart - paddingEnd) / (columnsCount / 2 + 2 * (columnsCount / 2f).roundToInt()))
        itemHeight = (h - paddingTop - paddingBottom) / (rowsCount * 2 - 1)
        prepareRectList()
    }

    private fun prepareRectList() {
        var prevRowRect: RectF? = null
        repeat(rowsCount) { row ->
            var prevColumnRect: RectF? = null
            repeat(columnsCount) { column ->
                val index = columnsCount * row + column
                prevColumnRect = getRect(row, column).apply {
                    val left = prevColumnRect?.right ?: paddingStart
                    val top = prevRowRect?.run { bottom + itemHeight } ?: paddingTop
                    val width = when {
                        prevColumnRect == null && row % 2 == 0 -> itemWidth
                        prevColumnRect == null && row % 2 == 1 -> (itemWidth / 4 * 3)
                        prevColumnRect != null && index % 2 == 0 -> itemWidth
                        else -> itemWidth.half
                    }
                    set(
                        left.toFloat(),
                        top.toFloat(),
                        left.toFloat() + width,
                        top.toFloat() + itemHeight
                    )
                }
            }
            prevRowRect = getRect(row, 0)
        }
    }

    override fun onDraw(canvas: Canvas) {
        rectList.forEachIndexed { index, rect ->
            if (index% 2 == 0) canvas.drawRoundRect(rect, radius, radius, paint)
        }
    }

    private fun getRect(row: Int, column: Int) = rectList[columnsCount * row + column]
}

private const val HORIZONTAL_COUNT = 8
private const val VERTICAL_COUNT = 7
private const val CORNERS_DP = 7
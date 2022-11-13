package com.chekurda.design.custom_view_tools.utils.view_extensions

import android.graphics.Paint.FontMetrics
import android.text.TextPaint
import com.nhaarman.mockitokotlin2.atLeastOnce
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.only
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import com.chekurda.design.custom_view_tools.utils.getTextWidth
import com.chekurda.design.custom_view_tools.utils.textHeight

/**
 * Тесты методов [getTextWidth] и [textHeight].
 *
 * @author vv.chekurda
 */
@RunWith(JUnitParamsRunner::class)
class CustomViewExtensionsKtTextPaintExtensionsTest {

    private val mockTextPaint: TextPaint = mock()

    @Test
    fun `When call getTextWidth(), then calls measureText with CharSequence arg`() {
        val text: CharSequence = "Test string"
        whenever(mockTextPaint.measureText(text, 0, text.length)).thenReturn(1f)

        mockTextPaint.getTextWidth(text)

        verify(mockTextPaint, only()).measureText(text, 0, text.length)
    }

    @Test
    @Parameters(value = [
        "1f|-1f|2",
        "1.1f|-1f|3",
        "1.7f|-1f|3"
    ])
    fun `When call getTextHeight(), then result is ceil text height`(descent: Float, ascent: Float, expected: Int) {
        val fontMetrics = FontMetrics().also {
            it.descent = descent
            it.ascent = ascent
        }
        whenever(mockTextPaint.fontMetrics).thenReturn(fontMetrics)

        val result = mockTextPaint.textHeight

        assertEquals(expected, result)
        verify(mockTextPaint, atLeastOnce()).fontMetrics
    }
}
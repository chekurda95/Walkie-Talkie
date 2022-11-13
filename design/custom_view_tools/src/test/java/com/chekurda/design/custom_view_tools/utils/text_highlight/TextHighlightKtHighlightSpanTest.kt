package com.chekurda.design.custom_view_tools.utils.text_highlight

import android.graphics.Color
import android.text.Spannable
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.junit.MockitoJUnitRunner
import com.chekurda.design.custom_view_tools.utils.setHighlightSpan

/**
 * Тесты метода [setHighlightSpan].
 *
 * @author vv.chekurda
 */
@RunWith(MockitoJUnitRunner.StrictStubs::class)
class TextHighlightKtHighlightSpanTest {

    @Mock
    private lateinit var spannable: Spannable
    private val highlightColor = Color.YELLOW

    @Test
    fun `When end position bigger than start position, then set span`() {
        val start = 0
        val end = 5

        spannable.setHighlightSpan(highlightColor, start, end)

        verify(spannable).setSpan(any(), eq(start), eq(end), anyInt())
        verifyNoMoreInteractions(spannable)
    }

    @Test
    fun `When end position smaller than start position, then don't set span`() {
        val start = 5
        val end = 0

        spannable.setHighlightSpan(highlightColor, start, end)

        verifyZeroInteractions(spannable)
    }

    @Test
    fun `When end position equals start position, then don't set span`() {
        val start = 5
        val end = 5

        spannable.setHighlightSpan(highlightColor, start, end)

        verifyZeroInteractions(spannable)
    }
}
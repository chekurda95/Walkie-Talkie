package com.chekurda.design.custom_view_tools.utils.text_highlight

import android.graphics.Color
import android.os.Build
import android.text.Spannable
import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.same
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import org.apache.commons.lang3.StringUtils.EMPTY
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyInt
import com.chekurda.design.custom_view_tools.utils.HighlightSpan
import com.chekurda.design.custom_view_tools.utils.TextHighlights
import com.chekurda.design.custom_view_tools.utils.highlightText
import com.chekurda.design.custom_view_tools.utils.lastTextIndex
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Тесты метода [highlightText].
 *
 * @author vv.chekurda
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.P])
class TextHighlightKtHighlightTextTest {

    private val highlightColor = Color.YELLOW

    @Test
    fun `When text is empty, then return this text`() {
        val text = EMPTY
        val highlightSpanList = listOf(HighlightSpan(0, 1))
        val highlights = TextHighlights(highlightSpanList, highlightColor)

        val result = text.highlightText(highlights)

        assertEquals(text, result)
        assertTrue(text.isEmpty())
    }

    @Test
    fun `When highlight span list is null, then return this text`() {
        val text = "Test string"
        val highlightSpanList = null
        val highlights = TextHighlights(highlightSpanList, highlightColor)

        val result = text.highlightText(highlights)

        assertEquals(text, result)
        assertNull(highlights.positionList)
    }

    @Test
    fun `When highlight span list is empty, then return this text`() {
        val text = "Test string"
        val highlightSpanList = listOf<HighlightSpan>()
        val highlights = TextHighlights(highlightSpanList, highlightColor)

        val result = text.highlightText(highlights)

        assertEquals(text, result)
        assertTrue(highlights.positionList!!.isEmpty())
    }

    @Test
    fun `When text is not spannable, then make it spannable`() {
        val text: CharSequence = "Test string"
        val highlightSpanList = listOf(HighlightSpan(0, 1))
        val highlights = TextHighlights(highlightSpanList, highlightColor)

        val result = text.highlightText(highlights)

        assertTrue(result is Spannable)
        assertFalse(text is Spannable)
        assertNotEquals(result, text)
    }

    @Test
    fun `When highlight positions is inside text length, then highlight text`() {
        val mockText = mock<Spannable> {
            on { length } doReturn 10
        }
        val insideSpanList = listOf(
            HighlightSpan(0, 1),
            HighlightSpan(3, 5),
            HighlightSpan(5, 9)
        )
        val highlights = TextHighlights(insideSpanList, highlightColor)

        mockText.highlightText(highlights)

        verify(mockText).setSpan(anyOrNull(), same(insideSpanList[0].start), same(insideSpanList[0].end), anyInt())
        verify(mockText).setSpan(anyOrNull(), same(insideSpanList[1].start), same(insideSpanList[1].end), anyInt())
        verify(mockText).setSpan(anyOrNull(), same(insideSpanList[2].start), same(insideSpanList[2].end), anyInt())
        verify(mockText, times(insideSpanList.size)).setSpan(anyOrNull(), anyInt(), anyInt(), anyInt())
    }

    @Test
    fun `When highlight positions is outside text length, then not highlight text`() {
        val mockText = mock<Spannable> {
            on { length } doReturn 10
        }
        val outsideSpanList = listOf(
            HighlightSpan(10, 11),
            HighlightSpan(20, 30),
            HighlightSpan(100, 200)
        )
        val highlights = TextHighlights(outsideSpanList, highlightColor)

        mockText.highlightText(highlights)

        verify(mockText, never()).setSpan(anyOrNull(), anyInt(), anyInt(), anyInt())
    }

    @Test
    fun `When text has simple ellipsize and highlight positions is outside text length, then highlight only ellipsize`() {
        val text = "Test..."
        val lastTextIndex = text.lastTextIndex
        val ellipsizeFirstIndex = text.lastTextIndex + 1
        val outsideSpanList = listOf(
            HighlightSpan(10, 11),
            HighlightSpan(20, 30)
        )
        val highlights = TextHighlights(outsideSpanList, highlightColor)

        val result = text.highlightText(highlights) as Spannable
        val textSpans = result.getSpans(0, ellipsizeFirstIndex, Any::class.java)
        val ellipsizeSpans = result.getSpans(ellipsizeFirstIndex, text.length, Any::class.java)

        assertEquals(0, textSpans.size)
        assertEquals(1, ellipsizeSpans.size)
        assertEquals('t', text[lastTextIndex])
        assertEquals('.', text[ellipsizeFirstIndex])
    }

    @Test
    fun `When text has char ellipsize and highlight positions is outside text length, then highlight only ellipsize`() {
        val text = "Test…"
        val lastTextIndex = text.lastTextIndex
        val ellipsizeIndex = text.lastTextIndex + 1
        val outsideSpanList = listOf(
            HighlightSpan(10, 11),
            HighlightSpan(20, 30)
        )
        val highlights = TextHighlights(outsideSpanList, highlightColor)

        val result = text.highlightText(highlights) as Spannable
        val textSpans = result.getSpans(0, lastTextIndex, Any::class.java)
        val ellipsizeSpans = result.getSpans(lastTextIndex, text.length, Any::class.java)

        assertEquals(0, textSpans.size)
        assertEquals(1, ellipsizeSpans.size)
        assertEquals('t', text[lastTextIndex])
        assertEquals('…', text[ellipsizeIndex])
    }

    @Test
    fun `When text has ellipsize, highlight positions is inside and outside text length, then highlight text and ellipsize`() {
        val text = "Test text…"
        val lastTextIndex = text.lastTextIndex
        val insideSpanList = listOf(
            HighlightSpan(0, 1),
            HighlightSpan(3, 4)
        )
        val outsideSpanList = listOf(HighlightSpan(20, 30))
        val highlightSpanList = mutableListOf<HighlightSpan>().apply {
            addAll(insideSpanList)
            addAll(outsideSpanList)
        }
        val highlights = TextHighlights(highlightSpanList, highlightColor)

        val result = text.highlightText(highlights) as Spannable
        val textSpans = result.getSpans(0, lastTextIndex, Any::class.java)
        val ellipsizeSpans = result.getSpans(lastTextIndex, text.length, Any::class.java)

        assertEquals(2, textSpans.size)
        assertEquals(1, ellipsizeSpans.size)
    }
}
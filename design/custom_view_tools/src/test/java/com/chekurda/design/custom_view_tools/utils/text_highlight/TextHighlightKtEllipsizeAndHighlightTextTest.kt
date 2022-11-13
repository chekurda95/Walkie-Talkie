package com.chekurda.design.custom_view_tools.utils.text_highlight

import android.graphics.Color
import android.os.Build
import android.text.TextPaint
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.apache.commons.lang3.StringUtils.EMPTY
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import com.chekurda.design.custom_view_tools.utils.HighlightSpan
import com.chekurda.design.custom_view_tools.utils.TextHighlights
import com.chekurda.design.custom_view_tools.utils.ellipsizeAndHighlightText
import com.chekurda.design.custom_view_tools.utils.highlightText
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Тесты метода [ellipsizeAndHighlightText].
 *
 * @author vv.chekurda
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.P])
class TextHighlightKtEllipsizeAndHighlightTextTest {

    private val textPaint = TextPaint()
    private val highlightColor = Color.YELLOW
    private val bigLayoutWidth = Integer.MAX_VALUE
    private val testText = "Test string"

    @Test
    fun `When text is blank, then return empty string`() {
        val blankString = "   "

        val result = blankString.ellipsizeAndHighlightText(textPaint, bigLayoutWidth, null)

        assertEquals(EMPTY, result)
        assertTrue(blankString.isBlank())
    }

    @Test
    fun `When width smaller than 0, then return empty string`() {
        val negativeLayoutWidth = -5

        val result = testText.ellipsizeAndHighlightText(textPaint, negativeLayoutWidth, null)

        assertEquals(EMPTY, result)
        assertTrue(testText.isNotEmpty())
    }

    @Test
    fun `When highlights is null, then return this text`() {
        val highlights = null

        val result = testText.ellipsizeAndHighlightText(textPaint, bigLayoutWidth, highlights)

        assertEquals(testText, result)
    }

    @Test
    fun `When highlight span list is empty, then return this text`() {
        val highlights = TextHighlights(listOf(), highlightColor)

        val result = testText.ellipsizeAndHighlightText(textPaint, bigLayoutWidth, highlights)

        assertEquals(testText, result)
        assertTrue(highlights.positionList!!.isEmpty())
    }

    @Test
    fun `When highlight span list is not empty, then call highlight text`() {
        val mockText = mock<CharSequence>()
        val listOfHighlights = listOf(HighlightSpan(0, 1))
        val highlights = TextHighlights(listOfHighlights, highlightColor)

        val result = mockText.ellipsizeAndHighlightText(textPaint, bigLayoutWidth, highlights)

        assertNotEquals(mockText, result)
        assertFalse(highlights.positionList.isNullOrEmpty())
        verify(mockText).highlightText(highlights)
    }
}
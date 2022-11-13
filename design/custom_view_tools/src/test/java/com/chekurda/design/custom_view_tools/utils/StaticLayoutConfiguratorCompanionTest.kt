package com.chekurda.design.custom_view_tools.utils

import android.graphics.Color
import android.graphics.Paint.FontMetrics
import android.os.Build
import android.text.Layout
import android.text.Layout.Alignment.ALIGN_NORMAL
import android.text.SpannableString
import android.text.TextPaint
import android.text.TextUtils.TruncateAt
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import org.apache.commons.lang3.StringUtils.EMPTY
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import com.chekurda.design.custom_view_tools.utils.StaticLayoutConfigurator.Companion.createStaticLayout
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Тесты [StaticLayoutConfigurator].
 *
 * @author vv.chekurda
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.P])
class StaticLayoutConfiguratorCompanionTest {

    private val textPaint = TextPaint()
    private val testText = "Test string"

    @Test
    fun `When width is default, then layout width is equals full text width`() {
        val expectedTextWidth = textPaint.getTextWidth(testText)

        val layout = createStaticLayout(testText, textPaint)

        assertEquals(expectedTextWidth, layout.width)
        assertNotEquals(0, layout.width)
        assertTrue(testText.isNotBlank())
    }

    @Test
    fun `When width is custom, then layout width is equals custom width`() {
        val customWidth = 200
        val fullTextWidth = textPaint.getTextWidth(testText)

        val layout = createStaticLayout(testText, textPaint) {
            width = customWidth
        }

        assertEquals(customWidth, layout.width)
        assertNotEquals(fullTextWidth, layout.width)
    }

    @Test
    fun `When custom width is negative, then layout width is equals 0`() {
        val customWidth = -50

        val layout = createStaticLayout(testText, textPaint) {
            width = customWidth
        }

        assertEquals(0, layout.width)
        assertTrue(testText.isNotBlank())
    }

    @Test
    fun `When custom width is bigger than text width, then layout width is equals custom width`() {
        val fullTextWidth = textPaint.getTextWidth(testText)
        val customWidth = fullTextWidth + 100

        val layout = createStaticLayout(testText, textPaint) {
            width = customWidth
        }

        assertEquals(customWidth, layout.width)
        assertTrue(layout.width > fullTextWidth)
        assertNotEquals(0, fullTextWidth)
    }

    @Test
    fun `When text is empty, then layout width is equals custom width`() {
        val text = EMPTY
        val customWidth = 50

        val layout = createStaticLayout(text, textPaint) {
            width = customWidth
        }

        assertEquals(customWidth, layout.width)
        assertTrue(text.isEmpty())
    }

    @Test
    fun `When max lines is custom and text is not blank, then configured max lines is equals custom max lines`() {
        val customMaxLines = 5
        val configurator = StaticLayoutConfigurator(testText, textPaint) {
            maxLines = customMaxLines
        }

        configurator.configure()

        assertEquals(customMaxLines, configurator.maxLines)
        assertTrue(testText.isNotBlank())
    }

    @Test
    fun `When custom max lines is equals 0, then configured max lines is equals single line`() {
        val customMaxLines = 0
        val configurator = StaticLayoutConfigurator(testText, textPaint) {
            maxLines = customMaxLines
        }

        configurator.configure()

        assertEquals(1, configurator.maxLines)
    }

    @Test
    fun `When custom max lines is negative, then configured max lines is equals single line`() {
        val customMaxLines = -5
        val configurator = StaticLayoutConfigurator(testText, textPaint) {
            maxLines = customMaxLines
        }

        configurator.configure()

        assertEquals(1, configurator.maxLines)
    }

    @Test
    fun `When text is blank, then custom max lines will be ignored and configured max lines is equals single line`() {
        val customMaxLines = 5
        val blankText = "   "
        val configurator = StaticLayoutConfigurator(blankText, textPaint) {
            maxLines = customMaxLines
        }

        configurator.configure()

        assertEquals(1, configurator.maxLines)
        assertTrue(blankText.isBlank())
    }

    @Test
    fun `When custom max height is set, then configured max lines is equals max height divided by text height`() {
        val textHeight = 10f
        val mockTextPaint = mockTextPaint(textHeight = textHeight)
        val customMaxHeight = 25
        val configurator = StaticLayoutConfigurator(testText, mockTextPaint) {
            maxHeight = customMaxHeight
        }

        configurator.configure()

        assertEquals(2, configurator.maxLines)
    }

    @Test
    fun `When custom max height is negative, then configured max lines is equals single line`() {
        val mockTextPaint = mockTextPaint(textHeight = 10f)
        val customMaxHeight = -50
        val configurator = StaticLayoutConfigurator(testText, mockTextPaint) {
            maxHeight = customMaxHeight
        }

        configurator.configure()

        assertEquals(1, configurator.maxLines)
    }

    @Test
    fun `When custom max height is smaller than one line, then configured max lines is equals 1`() {
        val textHeight = 20f
        val mockTextPaint = mockTextPaint(textHeight = textHeight)
        val customMaxHeight = 10
        val configurator = StaticLayoutConfigurator(testText, mockTextPaint) {
            maxHeight = customMaxHeight
        }

        configurator.configure()

        assertEquals(1, configurator.maxLines)
    }

    @Test
    fun `When custom max height is set, then custom max lines will be ignored`() {
        val mockTextPaint = mockTextPaint(textHeight = 10f)
        val customMaxHeight = 10
        val customLines = 20
        val configurator = StaticLayoutConfigurator(testText, mockTextPaint) {
            maxHeight = customMaxHeight
            maxLines = customLines
        }

        configurator.configure()

        assertNotEquals(customLines, configurator.maxLines)
    }

    @Test
    fun `When alignment is default, then layout alignment is equals ALIGN_NORMAL`() {
        val layout = createStaticLayout(testText, textPaint)

        assertEquals(ALIGN_NORMAL, layout.alignment)
    }

    @Test
    fun `When custom alignment is set, then layout alignment is equals custom alignment`() {
        val customAlignment = Layout.Alignment.ALIGN_CENTER

        val layout = createStaticLayout(testText, textPaint) {
            alignment = customAlignment
        }

        assertEquals(customAlignment, layout.alignment)
    }

    @Test
    fun `When ellipsize is default, then configured ellipsize is equals END`() {
        val configurator = StaticLayoutConfigurator(testText, textPaint)

        assertEquals(TruncateAt.END, configurator.ellipsize)
    }

    @Test
    fun `When ellipsize is custom, then configured ellipsize is equals custom ellipsize`() {
        val customEllipsize = TruncateAt.MARQUEE
        val configurator = StaticLayoutConfigurator(testText, textPaint) {
            ellipsize = customEllipsize
        }

        configurator.configure()

        assertEquals(customEllipsize, configurator.ellipsize)
    }

    @Test
    fun `Layout ellipsize width is equals configured width parameter`() {
        val customWidth = 50

        val layout = createStaticLayout(testText, textPaint) {
            width = customWidth
        }

        assertEquals(customWidth, layout.ellipsizedWidth)
    }

    @Test
    fun `When includeFontPad is default, then configured includeFontPad is equals true`() {
        val configurator = StaticLayoutConfigurator(testText, textPaint)

        configurator.configure()

        assertTrue(configurator.includeFontPad)
    }

    @Test
    fun `When includeFontPad is custom, then configured includeFontPad is equals custom includeFontPad`() {
        val configurator = StaticLayoutConfigurator(testText, textPaint) {
            includeFontPad = false
        }

        configurator.configure()

        assertFalse(configurator.includeFontPad)
    }

    @Test
    fun `When highlights is default, then configured highlights is null`() {
        val configurator = StaticLayoutConfigurator(testText, textPaint)

        configurator.configure()

        assertEquals(null, configurator.highlights)
    }

    @Test
    fun `When highlights is custom, then configured highlights is equals custom highlights`() {
        val customHighlights = TextHighlights(listOf(), Color.YELLOW)
        val configurator = StaticLayoutConfigurator(testText, textPaint) {
            highlights = customHighlights
        }

        configurator.configure()

        assertEquals(customHighlights, configurator.highlights)
    }

    @Test
    fun `When text highlights is not empty, then highlight span is applied`() {
        val highlightList = listOf(
            HighlightSpan(0, 1),
            HighlightSpan(2, 3)
        )
        val customHighlights = TextHighlights(highlightList, Color.YELLOW)

        val layout = createStaticLayout(testText, textPaint) {
            highlights = customHighlights
        }
        val spannable = SpannableString(layout.text)
        val spans = spannable.getSpans(0, spannable.length, Any::class.java)

        assertEquals(2, spans.size)
    }

    private fun mockTextPaint(textHeight: Float): TextPaint =
        mock {
            val fontMetrics = FontMetrics().apply {
                descent = textHeight
                ascent = 0f
            }
            on { this.fontMetrics } doReturn fontMetrics
        }
}
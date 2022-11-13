package com.chekurda.design.custom_view_tools.text_layout.size.height

import android.os.Build
import com.chekurda.design.custom_view_tools.TextLayout
import com.chekurda.design.custom_view_tools.TextLayout.TextLayoutPadding
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Тесты [TextLayout.height] при использовании [TextLayout.TextLayoutParams.maxHeight].
 *
 * @author vv.chekurda
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.P])
class TextLayoutMaxHeightTest {

    private lateinit var textLayout: TextLayout
    private var textHeight = 0

    @Before
    fun setUp() {
        textLayout = TextLayout().apply {
            buildLayout {
                text = StringBuilder()
                    .appendLine("1 line")
                    .appendLine("2 line")
                    .appendLine("3 line")
                    .append("4 line")
                paint.textSize = 100f
                maxLines = Int.MAX_VALUE
                includeFontPad = false
            }
        }
        textHeight = textLayout.state.cachedLayout!!.height
    }

    @Test
    fun `TextLayout has 4 lines`() {
        assertEquals(4, textLayout.lineCount)
    }

    @Test
    fun `When maxHeight is smaller than text height, then TextLayout's height is smaller or equals maxHeight`() {
        val maxHeight = textHeight / 2

        textLayout.buildLayout { this.maxHeight = maxHeight }

        assertTrue(textLayout.height <= maxHeight)
        assertTrue(textLayout.height > 0)
        assertTrue(maxHeight < textHeight)
    }

    @Test
    fun `When maxHeight is bigger than text height, then TextLayout's height is equals text height`() {
        val maxHeight = textHeight * 2

        textLayout.buildLayout { this.maxHeight = maxHeight }

        assertEquals(textHeight, textLayout.height)
        assertTrue(maxHeight > textHeight)
    }

    @Test
    fun `When maxHeight is smaller than multiline text height, then TextLayout's lineCount is equals max of availableLines`() {
        val segmentation = 2
        val maxHeight = textHeight / segmentation
        val expectedLinesCount = textLayout.lineCount / segmentation

        textLayout.buildLayout { this.maxHeight = maxHeight }

        assertEquals(expectedLinesCount, textLayout.lineCount)
        assertTrue(expectedLinesCount >= 1)
        assertTrue(maxHeight < textHeight)
    }

    @Test
    fun `When maxHeight is smaller than sum of padding and text height, then TextLayout's height is smaller or equals maxHeight`() {
        val layoutPadding = TextLayoutPadding(top = 100)
        val textHeightWithPadding = textHeight + layoutPadding.top
        val maxHeight = textHeightWithPadding / 2

        textLayout.buildLayout {
            this.maxHeight = maxHeight
            padding = layoutPadding
        }

        assertTrue(textLayout.height <= maxHeight)
        assertTrue(textLayout.height > 0)
        assertTrue(maxHeight < textHeightWithPadding)
        assertEquals(layoutPadding.top, textLayout.paddingTop)
    }

    @Test
    fun `When maxHeight is bigger than sum of padding and text height, then TextLayout's height is equals sum of padding and text height`() {
        val layoutPadding = TextLayoutPadding(top = 100)
        val textHeightWithPadding = textHeight + layoutPadding.top
        val maxHeight = textHeightWithPadding * 2

        textLayout.buildLayout {
            this.maxHeight = maxHeight
            padding = layoutPadding
        }

        assertEquals(textHeightWithPadding, textLayout.height)
        assertTrue(maxHeight > textHeightWithPadding)
    }
}
package com.chekurda.design.custom_view_tools.text_layout.size.width

import android.os.Build
import com.chekurda.design.custom_view_tools.TextLayout
import com.chekurda.design.custom_view_tools.TextLayout.TextLayoutPadding
import com.chekurda.design.custom_view_tools.utils.getTextWidth
import org.apache.commons.lang3.StringUtils.EMPTY
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Тесты [TextLayout.width] при использовании [TextLayout.TextLayoutParams.minWidth].
 *
 * @author vv.chekurda
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.P])
class TextLayoutMinWidthTest {

    private lateinit var textLayout: TextLayout
    private var textWidth = 0

    @Before
    fun setUp() {
        textLayout = TextLayout { text = "Test text" }
        textWidth = textLayout.textPaint.getTextWidth(textLayout.text)
    }

    @Test
    fun `When minWidth is bigger than text width, then TextLayout's width is equals minWidth`() {
        val minWidth = textWidth + 100

        textLayout.buildLayout { this.minWidth = minWidth }

        assertEquals(minWidth, textLayout.width)
        assertTrue(minWidth > textWidth)
    }

    @Test
    fun `When minWidth is smaller than text width, then TextLayout's width is equals text width`() {
        val minWidth = textWidth / 2

        textLayout.buildLayout { this.minWidth = minWidth }

        assertEquals(textWidth, textLayout.width)
        assertTrue(minWidth < textWidth)
    }

    @Test
    fun `When minWidth is bigger than layoutWidth, then TextLayout's width is equals layoutWidth`() {
        val layoutWidth = 50
        val minWidth = layoutWidth * 2

        textLayout.buildLayout {
            this.layoutWidth = layoutWidth
            this.minWidth = minWidth
        }

        assertEquals(layoutWidth, textLayout.width)
        assertTrue(minWidth > layoutWidth)
    }

    @Test
    fun `When minWidth is smaller than layoutWidth, then TextLayout's width is equals layoutWidth`() {
        val minWidth = 50
        val layoutWidth = minWidth * 2

        textLayout.buildLayout {
            this.minWidth = minWidth
            this.layoutWidth = layoutWidth
        }

        assertEquals(layoutWidth, textLayout.width)
        assertTrue(minWidth < layoutWidth)
    }

    @Test
    fun `When minWidth is set and text is empty, then TextLayout's width is equals minWidth`() {
        val minWidth = 100

        textLayout.buildLayout {
            text = EMPTY
            this.minWidth = minWidth
        }

        assertEquals(minWidth, textLayout.width)
        assertNotEquals(0, minWidth)
        assertTrue(textLayout.text.isEmpty())
    }

    @Test
    fun `When minWidth is set, text is empty and isVisibleWhenBlank is equals false, then TextLayout's width is equals 0`() {
        val minWidth = 100

        textLayout.buildLayout {
            text = EMPTY
            isVisibleWhenBlank = false
            this.minWidth = minWidth
        }

        assertEquals(0, textLayout.width)
        assertNotEquals(0, minWidth)
        assertTrue(textLayout.text.isEmpty())
    }

    @Test
    fun `When minWidth is set, isVisible is equals false and text is not blank, then TextLayout's width is equals 0`() {
        val minWidth = 100

        textLayout.buildLayout {
            isVisible = false
            this.minWidth = minWidth
        }

        assertEquals(0, textLayout.width)
        assertNotEquals(0, minWidth)
        assertTrue(textLayout.text.isNotBlank())
    }

    @Test
    fun `When minWidth is bigger than sum of padding and text width, then TextLayout's width is equals minWidth and includes padding`() {
        val paddingStart = 100
        val layoutPadding = TextLayoutPadding(start = paddingStart)
        val textWidthWithPadding = textWidth + paddingStart
        val minWidth = textWidthWithPadding * 2

        textLayout.buildLayout {
            this.minWidth = minWidth
            padding = layoutPadding
        }

        assertEquals(minWidth, textLayout.width)
        assertEquals(paddingStart, textLayout.paddingStart)
        assertTrue(minWidth > textWidthWithPadding)
    }

    @Test
    fun `When minWidth is smaller than sum of padding and text width, then TextLayout's width is equals sum of padding and text width`() {
        val paddingStart = 100
        val layoutPadding = TextLayoutPadding(start = 100)
        val textWidthWithPadding = textWidth + paddingStart
        val minWidth = textWidthWithPadding / 2

        textLayout.buildLayout {
            this.minWidth = minWidth
            padding = layoutPadding
        }

        assertEquals(textWidthWithPadding, textLayout.width)
        assertTrue(minWidth < textWidthWithPadding)
    }

    @Test
    fun `When minWidth is bigger than text width, then StaticLayout's width is equals minWidth`() {
        val minWidth = textWidth * 2

        textLayout.buildLayout { this.minWidth = minWidth }

        assertEquals(minWidth, textLayout.state.cachedLayout?.width)
        assertTrue(minWidth > textWidth)
    }

    @Test
    fun `When minWidth is smaller than text width, then StaticLayout's width is equals text width`() {
        val minWidth = textWidth / 2

        textLayout.buildLayout { this.minWidth = minWidth }

        assertEquals(textWidth, textLayout.state.cachedLayout?.width)
        assertTrue(minWidth < textWidth)
    }

    @Test
    fun `When minWidth is bigger than maxWidth, then TextLayout's width is equals minWidth`() {
        val minWidth = 50
        val maxWidth = minWidth / 2

        textLayout.buildLayout {
            this.minWidth = minWidth
            this.maxWidth = maxWidth
        }

        assertEquals(minWidth, textLayout.width)
    }
}
package com.chekurda.design.custom_view_tools.text_layout.size.width

import android.os.Build
import com.chekurda.design.custom_view_tools.TextLayout
import com.chekurda.design.custom_view_tools.TextLayout.TextLayoutPadding
import com.chekurda.design.custom_view_tools.utils.getTextWidth
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Тесты [TextLayout.width] при использовании [TextLayout.TextLayoutParams.maxWidth].
 *
 * @author vv.chekurda
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.P])
class TextLayoutMaxWidthTest {

    private lateinit var textLayout: TextLayout
    private var textWidth = 0

    @Before
    fun setUp() {
        textLayout = TextLayout { text = "Test text" }
        textWidth = textLayout.textPaint.getTextWidth(textLayout.text)
    }

    @Test
    fun `When maxWidth is smaller than text width, then TextLayout's width is equals maxWidth`() {
        val maxWidth = textWidth / 2

        textLayout.buildLayout { this.maxWidth = maxWidth }

        assertEquals(maxWidth, textLayout.width)
        assertTrue(maxWidth < textWidth)
    }

    @Test
    fun `When maxWidth is bigger than text width, then TextLayout's width is equals text width`() {
        val maxWidth = textWidth + 100

        textLayout.buildLayout { this.maxWidth = maxWidth }

        assertEquals(textWidth, textLayout.width)
        assertTrue(maxWidth > textWidth)
    }

    @Test
    fun `When maxWidth is smaller than layoutWidth, then TextLayout's width is equals layoutWidth`() {
        val maxWidth = 50
        val layoutWidth = maxWidth * 2

        textLayout.buildLayout {
            this.maxWidth = maxWidth
            this.layoutWidth = layoutWidth
        }

        assertEquals(layoutWidth, textLayout.width)
        assertTrue(maxWidth < layoutWidth)
    }

    @Test
    fun `When maxWidth is bigger than layoutWidth, then TextLayout's width is equals layoutWidth`() {
        val layoutWidth = 50
        val maxWidth = layoutWidth * 2

        textLayout.buildLayout {
            this.layoutWidth = layoutWidth
            this.maxWidth = maxWidth
        }

        assertEquals(layoutWidth, textLayout.width)
        assertTrue(maxWidth > layoutWidth)
    }

    @Test
    fun `When maxWidth is smaller than sum of padding and text width, then TextLayout's width is equals maxWidth and includes padding`() {
        val paddingStart = 100
        val layoutPadding = TextLayoutPadding(start = paddingStart)
        val textWidthWithPadding = textWidth + paddingStart
        val maxWidth = textWidthWithPadding / 2

        textLayout.buildLayout {
            this.maxWidth = maxWidth
            padding = layoutPadding
        }

        assertEquals(maxWidth, textLayout.width)
        assertEquals(paddingStart, textLayout.paddingStart)
        assertTrue(maxWidth < textWidthWithPadding)
    }

    @Test
    fun `When maxWidth is bigger than sum of padding and text width, then TextLayout's width is equals sum of padding and text width`() {
        val paddingStart = 100
        val layoutPadding = TextLayoutPadding(start = paddingStart)
        val textWidthWithPadding = textWidth + paddingStart
        val maxWidth = textWidthWithPadding * 2

        textLayout.buildLayout {
            this.maxWidth = maxWidth
            padding = layoutPadding
        }

        assertEquals(textWidthWithPadding, textLayout.width)
        assertTrue(maxWidth > textWidthWithPadding)
    }

    @Test
    fun `When maxWidth is smaller than text width, then StaticLayout's width is equals maxWidth`() {
        val maxWidth = textWidth / 2

        textLayout.buildLayout { this.maxWidth = maxWidth }

        assertEquals(maxWidth, textLayout.state.cachedLayout?.width)
        assertTrue(maxWidth < textWidth)
    }

    @Test
    fun `When maxWidth is bigger than text width, then StaticLayout's width is equals text width`() {
        val maxWidth = textWidth * 2

        textLayout.buildLayout { this.maxWidth = maxWidth }

        assertEquals(textWidth, textLayout.state.cachedLayout?.width)
        assertTrue(maxWidth > textWidth)
    }
}
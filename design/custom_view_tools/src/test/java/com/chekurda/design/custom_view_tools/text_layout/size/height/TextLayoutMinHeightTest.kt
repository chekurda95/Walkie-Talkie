package com.chekurda.design.custom_view_tools.text_layout.size.height

import android.os.Build
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.chekurda.design.custom_view_tools.TextLayout
import com.chekurda.design.custom_view_tools.TextLayout.TextLayoutPadding
import org.apache.commons.lang3.StringUtils.EMPTY
import org.junit.Assert.*
import org.junit.Before

import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/**
 * Тесты [TextLayout.height] при использовании [TextLayout.TextLayoutParams.minHeight].
 *
 * @author vv.chekurda
 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.P])
class TextLayoutMinHeightTest {

    private lateinit var textLayout: TextLayout
    private var textHeight = 0

    @Before
    fun setUp() {
        textLayout = TextLayout().apply {
            buildLayout {
                text = "Test text"
                paint.textSize = 100f
            }
        }
        textHeight = textLayout.state.cachedLayout!!.height
    }

    @Test
    fun `When minHeight is bigger than text height, then TextLayout's height is equals minHeight`() {
        val minHeight = textHeight * 2

        textLayout.buildLayout { this.minHeight = minHeight }

        assertEquals(minHeight, textLayout.height)
        assertTrue(minHeight > textHeight)
    }

    @Test
    fun `When minHeight is smaller than text height, then TextLayout's height is equals text height`() {
        val minHeight = textHeight / 2

        textLayout.buildLayout { this.minHeight = minHeight }

        assertEquals(textHeight, textLayout.height)
        assertTrue(minHeight < textHeight)
    }

    @Test
    fun `When minHeight is set and text is empty, then TextLayout's height is equals minHeight`() {
        val minHeight = 100

        textLayout.buildLayout {
            text = EMPTY
            this.minHeight = minHeight
        }

        assertEquals(minHeight, textLayout.height)
        assertNotEquals(0, minHeight)
        assertTrue(textLayout.text.isEmpty())
    }

    @Test
    fun `When minHeight is set, text is empty and isVisibleWhenBlank is equals false, then TextLayout's height is equals 0`() {
        val minHeight = 100

        textLayout.buildLayout {
            text = EMPTY
            isVisibleWhenBlank = false
            this.minHeight = minHeight
        }

        assertEquals(0, textLayout.height)
        assertNotEquals(0, minHeight)
        assertTrue(textLayout.text.isEmpty())
    }

    @Test
    fun `When minHeight is set, isVisible is equals false and text is not blank, then TextLayout's height is equals 0`() {
        val minHeight = 100

        textLayout.buildLayout {
            isVisible = false
            this.minHeight = minHeight
        }

        assertEquals(0, textLayout.height)
        assertNotEquals(0, minHeight)
        assertTrue(textLayout.text.isNotBlank())
    }

    @Test
    fun `When minHeight is bigger than sum of padding and text height, then TextLayout's height is equals minHeight`() {
        val layoutPadding = TextLayoutPadding(top = 100)
        val staticHeightWithPadding = textHeight + layoutPadding.top
        val minHeight = staticHeightWithPadding * 2

        textLayout.buildLayout {
            this.minHeight = minHeight
            padding = layoutPadding
        }

        assertEquals(minHeight, textLayout.height)
        assertEquals(layoutPadding.top, textLayout.paddingTop)
        assertTrue(minHeight > staticHeightWithPadding)
    }

    @Test
    fun `When minHeight is smaller than sum of padding and text height, then TextLayout's height is equals sum of padding and text height`() {
        val layoutPadding = TextLayoutPadding(top = 100)
        val staticHeightWithPadding = textHeight + layoutPadding.top
        val minHeight = staticHeightWithPadding / 2

        textLayout.buildLayout {
            this.minHeight = minHeight
            padding = layoutPadding
        }

        assertEquals(staticHeightWithPadding, textLayout.height)
        assertTrue(minHeight < staticHeightWithPadding)
    }

    @Test
    fun `When minHeight is bigger than maxHeight, then TextLayout's height is equals minHeight`() {
        val minHeight = 200
        val maxHeight = minHeight / 2

        textLayout.buildLayout {
            this.minHeight = minHeight
            this.maxHeight = maxHeight
        }

        assertEquals(minHeight, textLayout.height)
    }
}
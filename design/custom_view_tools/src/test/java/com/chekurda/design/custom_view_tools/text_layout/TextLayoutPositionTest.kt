package com.chekurda.design.custom_view_tools.text_layout

import android.os.Build
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.chekurda.design.custom_view_tools.TextLayout
import com.chekurda.design.custom_view_tools.utils.getTextWidth
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Тесты для расчета позиций [TextLayout] и позиций для текста.
 * @see TextLayout.left
 * @see TextLayout.top
 * @see TextLayout.right
 * @see TextLayout.bottom
 * @see TextLayout.textPos
 * @see TextLayout.baseline
 *
 * @author vv.chekurda
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.P])
class TextLayoutBuildLayoutTest {

    private lateinit var textLayout: TextLayout

    @Before
    fun setUp() {
        textLayout = TextLayout { text = "Test text" }
    }

    @Test
    fun `Default left position is equals 0`() {
        assertEquals(0, textLayout.left)
    }

    @Test
    fun `Default top position is equals 0`() {
        assertEquals(0, textLayout.top)
    }

    @Test
    fun `Default right position is equals 0`() {
        assertEquals(0, textLayout.right)
    }

    @Test
    fun `Default bottom position is equals 0`() {
        assertEquals(0, textLayout.bottom)
    }

    @Test
    fun `When call layout(), then left and top positions is equals positions from layout()`() {
        val left = 500
        val top = 1000

        textLayout.layout(left, top)

        assertEquals(left, textLayout.left)
        assertEquals(top, textLayout.top)
    }

    @Test
    fun `When call layout() and text is empty, then all positions is equals positions from layout()`() {
        textLayout.configure { text = "" }
        val left = 500
        val top = 1000


        textLayout.layout(left, top)

        assertEquals(left, textLayout.left)
        assertEquals(left, textLayout.right)
        assertEquals(top, textLayout.top)
        assertEquals(top, textLayout.bottom)
        assertTrue(textLayout.text.isEmpty())
    }

    @Test
    fun `When call layout() and text isn't empty, then right is equals left + text width`() {
        val width = textLayout.width
        val left = 1000
        val expectedPosition = left + width

        textLayout.layout(left, 0)

        assertEquals(expectedPosition, textLayout.right)
        assertNotEquals(0, width)
        assertTrue(textLayout.text.isNotEmpty())
    }

    @Test
    fun `When call layout() and text isn't empty, then bottom is equals top + text height`() {
        val height = textLayout.height
        val top = 1000
        val expectedPosition = top + height

        textLayout.layout(0, top)

        assertEquals(expectedPosition, textLayout.bottom)
        assertNotEquals(0, expectedPosition)
        assertTrue(textLayout.text.isNotEmpty())
    }

    @Test
    fun `When call layout() and text is empty, then right and bottom positions is equals 0`() {
        textLayout.configure { text = "" }

        textLayout.layout(0, 0)

        assertEquals(0, textLayout.right)
        assertEquals(0, textLayout.bottom)
        assertTrue(textLayout.text.isEmpty())
    }

    @Test
    fun `When call layout() and isVisible is equals false, then right and bottom positions is equals left and top`() {
        textLayout.configure { isVisible = false }
        val textWidth = textLayout.textPaint.getTextWidth(textLayout.text)
        val left = 1000
        val top = 200

        textLayout.layout(left, top)

        assertEquals(left, textLayout.right)
        assertEquals(top, textLayout.bottom)
        assertNotEquals(0, textWidth)
        assertFalse(textLayout.isVisible)
    }

    @Test
    fun `Text position is equals 0 before first call layout()`() {
        val paddingStart = 50
        val paddingTop = 75

        textLayout.configure {
            padding = TextLayout.TextLayoutPadding(start = paddingStart, top = paddingTop)
        }
        val textPos = textLayout.state.textPos

        assertEquals(0, textPos.first.toInt())
        assertEquals(0, textPos.second.toInt())
    }

    @Test
    fun `Text position has a padding offset after call layout()`() {
        val paddingStart = 50
        val paddingTop = 75

        textLayout.configure {
            padding = TextLayout.TextLayoutPadding(start = paddingStart, top = paddingTop)
        }
        textLayout.layout(0, 0)
        val textPos = textLayout.state.textPos

        assertEquals(paddingStart, textPos.first.toInt())
        assertEquals(paddingTop, textPos.second.toInt())
    }

    @Test
    fun `TextLayout baseline has StaticLayout baseline`() {
        val layoutBaseline = textLayout.baseline
        val staticBaseline = textLayout.state.cachedLayout!!.getLineBaseline(0)

        assertEquals(staticBaseline, layoutBaseline)
        assertNotEquals(0, layoutBaseline)
    }

    @Test
    fun `TextLayout baseline has a padding offset`() {
        val baselineBeforePadding = textLayout.baseline
        val topPadding = 50

        textLayout.updatePadding(top = topPadding)

        assertEquals(baselineBeforePadding + topPadding, textLayout.baseline)
        assertNotEquals(0, baselineBeforePadding)
    }

    @Test
    fun `When TextLayout isVisible is equals false, then baseline is equals StaticLayout baseline`() {
        val layoutBaseline = textLayout.baseline
        val staticBaseline = textLayout.state.cachedLayout!!.getLineBaseline(0)

        textLayout.buildLayout { isVisible = false }

        assertEquals(staticBaseline, layoutBaseline)
        assertNotEquals(0, layoutBaseline)
    }
}
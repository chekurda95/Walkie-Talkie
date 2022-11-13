package com.chekurda.design.custom_view_tools.text_layout.size.height

import android.os.Build
import com.chekurda.design.custom_view_tools.TextLayout
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
 * Тесты [TextLayout.height].
 *
 * @author vv.chekurda
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.P])
class TextLayoutHeightTest {

    private lateinit var textLayout: TextLayout

    @Before
    fun setUp() {
        textLayout = TextLayout { text = "Test text" }
    }

    @Test
    fun `TextLayout height is equals StaticLayout height`() {
        textLayout.buildLayout()
        val staticHeight = textLayout.state.cachedLayout!!.height
        val layoutHeight = textLayout.height

        assertEquals(staticHeight, layoutHeight)
        assertNotEquals(0, layoutHeight)
    }

    @Test
    fun `TextLayout height is equals 0, when isVisible is equals false`() {
        textLayout.configure { isVisible = false }
        val layoutHeight = textLayout.height

        assertEquals(0, layoutHeight)
        assertFalse(textLayout.isVisible)
    }

    @Test
    fun `TextLayout height is equals 0, when width is equals 0`() {
        textLayout.configure { layoutWidth = 0 }
        val layoutHeight = textLayout.height

        assertEquals(0, layoutHeight)
        assertEquals(0, textLayout.width)
    }

    @Test
    fun `TextLayout height is equals 0, when text is empty`() {
        textLayout.configure { text = "" }
        val layoutHeight = textLayout.height

        assertEquals(0, layoutHeight)
        assertTrue(textLayout.text.isEmpty())
    }

    @Test
    fun `TextLayout height is equals StaticLayout height + vertical padding, when padding is exists`() {
        textLayout.buildLayout()
        val paddingTop = 25
        val paddingBottom = 50
        val staticHeight = textLayout.state.cachedLayout!!.height
        val expectedHeight = staticHeight + paddingTop + paddingBottom

        textLayout.updatePadding(top = paddingTop, bottom = paddingBottom)

        assertEquals(expectedHeight, textLayout.height)
        assertNotEquals(0, expectedHeight)
    }

    @Test
    fun `TextLayout height is equals 0, when isVisible is equals false and padding is exists`() {
        textLayout.buildLayout { isVisible = false }
        textLayout.updatePadding(top = 25, bottom = 50)

        assertEquals(0, textLayout.height)
        assertNotEquals(0, textLayout.paddingTop)
        assertNotEquals(0, textLayout.paddingBottom)
        assertFalse(textLayout.isVisible)
    }

    @Test
    fun `TextLayout height is equals 0, when text is empty and padding is exists`() {
        textLayout.buildLayout { text = "" }
        textLayout.updatePadding(top = 25, bottom = 50)

        assertEquals(0, textLayout.height)
        assertTrue(textLayout.text.isEmpty())
        assertNotEquals(0, textLayout.paddingTop)
        assertNotEquals(0, textLayout.paddingBottom)
    }
}
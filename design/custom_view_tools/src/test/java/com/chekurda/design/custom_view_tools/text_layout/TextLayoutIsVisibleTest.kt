package com.chekurda.design.custom_view_tools.text_layout

import android.os.Build
import com.chekurda.design.custom_view_tools.TextLayout
import com.chekurda.design.custom_view_tools.TextLayout.TextLayoutParams
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
 * Тесты [TextLayout.isVisible].
 *
 * @author vv.chekurda
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.P])
class TextLayoutIsVisibleTest {

    private lateinit var textLayout: TextLayout

    @Before
    fun setUp() {
        textLayout = TextLayout { text = "Test text" }
    }

    @Test
    fun `Default isVisible is equals true`() {
        assertTrue(TextLayout().isVisible)
    }

    @Test
    fun `When isVisible is equals false, then TextLayout width is equals 0`() {
        val widthBeforeChanges = textLayout.width

        textLayout.configure { isVisible = false }

        assertEquals(0, textLayout.width)
        assertNotEquals(0, widthBeforeChanges)
    }

    @Test
    fun `When isVisible is equals false, then TextLayout height is equals 0`() {
        val heightBeforeChanges = textLayout.height

        textLayout.configure { isVisible = false }

        assertEquals(0, textLayout.height)
        assertNotEquals(0, heightBeforeChanges)
    }

    @Test
    fun `Default isVisibleWhenBlank is equals true`() {
        assertTrue(TextLayoutParams().isVisibleWhenBlank)
    }

    @Test
    fun `TextLayout isVisible is equals true, when isVisibleWhenBlank is equals true`() {
        textLayout.buildLayout { isVisibleWhenBlank = true }

        assertTrue(textLayout.isVisible)
    }

    @Test
    fun `TextLayout isVisible is equals true, when isVisibleWhenBlank is equals false and text is not blank`() {
        textLayout.buildLayout { isVisibleWhenBlank = false }

        assertTrue(textLayout.isVisible)
        assertTrue(textLayout.text.isNotBlank())
    }

    @Test
    fun `TextLayout isVisible is equals false, when isVisibleWhenBlank is equals false and text is blank`() {
        textLayout.buildLayout {
            text = "               "
            isVisibleWhenBlank = false
        }

        assertFalse(textLayout.isVisible)
        assertTrue(textLayout.text.isBlank())
    }
}
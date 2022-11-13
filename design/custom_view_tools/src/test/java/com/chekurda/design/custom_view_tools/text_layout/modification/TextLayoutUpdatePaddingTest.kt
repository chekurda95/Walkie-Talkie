package com.chekurda.design.custom_view_tools.text_layout.modification

import android.os.Build
import com.chekurda.design.custom_view_tools.TextLayout
import com.chekurda.design.custom_view_tools.TextLayout.TextLayoutPadding
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Тесты метода [TextLayout.updatePadding].
 *
 * @author vv.chekurda
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.P])
class TextLayoutUpdatePaddingTest {

    private lateinit var textLayout: TextLayout
    private val layoutPadding = TextLayoutPadding(1, 1, 1, 1)

    @Before
    fun setUp() {
        textLayout = TextLayout { padding = layoutPadding }
    }

    @Test
    fun `Call updatePadding returns false, when padding isn't changed`() {
        val padding = layoutPadding.copy()

        val isChanged = textLayout.updatePadding(padding.start, padding.top, padding.end, padding.bottom)

        assertFalse(isChanged)
    }

    @Test
    fun `Call updatePadding returns true, when padding is changed`() {
        val padding = layoutPadding.copy(start = 5)

        val isChanged = textLayout.updatePadding(padding.start, padding.top, padding.end, padding.bottom)

        assertTrue(isChanged)
    }

    @Test
    fun `When padding isn't changed, then isLayoutChanged is equals false`() {
        textLayout.buildLayout()
        assertFalse(textLayout.state.isLayoutChanged)

        val padding = layoutPadding.copy()
        textLayout.updatePadding(padding.start, padding.top, padding.end, padding.bottom)

        assertFalse(textLayout.state.isLayoutChanged)
    }

    @Test
    fun `When padding is changed, then isLayoutChanged is equals true`() {
        textLayout.buildLayout()
        assertFalse(textLayout.state.isLayoutChanged)

        val padding = layoutPadding.copy(start = 5)
        textLayout.updatePadding(padding.start, padding.top, padding.end, padding.bottom)

        assertTrue(textLayout.state.isLayoutChanged)
    }
}
package com.chekurda.design.custom_view_tools.text_layout.modification

import android.os.Build
import com.chekurda.design.custom_view_tools.TextLayout
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Тесты метода [TextLayout.buildLayout].
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
    fun `isLayoutChanged is equals false after call buildLayout()`() {
        val isLayoutChanged = textLayout.state.isLayoutChanged

        textLayout.buildLayout()

        assertFalse(textLayout.state.isLayoutChanged)
        assertTrue(isLayoutChanged)
    }

    @Test
    fun `When isVisible is equals false, then isLayoutChanged is equals true after call buildLayout()`() {
        textLayout.configure { isVisible = false }
        val isLayoutChanged = textLayout.state.isLayoutChanged

        textLayout.buildLayout()

        assertTrue(textLayout.state.isLayoutChanged)
        assertTrue(isLayoutChanged)
    }
}

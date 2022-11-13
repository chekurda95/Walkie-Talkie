package com.chekurda.design.custom_view_tools.text_layout.modification

import com.chekurda.design.custom_view_tools.TextLayout
import com.chekurda.design.custom_view_tools.TextLayoutConfig
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Тесты результата метода [TextLayout.buildLayout].
 *
 * @author vv.chekurda
 */
class TextLayoutBuildLayoutResultTest {

    private lateinit var mockTextLayout: TextLayout

    @Before
    fun setUp() {
        mockTextLayout = mock()
        whenever(mockTextLayout.buildLayout(any())).thenCallRealMethod()
    }

    @Test
    fun `When config is null, then buildLayout() is returns false`() {
        val configureResult = true
        whenever(mockTextLayout.configure(any())).thenReturn(configureResult)

        assertFalse(mockTextLayout.buildLayout())
    }

    @Test
    fun `buildLayout() is always returns configure result`() {
        val config = mock<TextLayoutConfig>()
        whenever(mockTextLayout.configure(any())).thenReturn(true)

        assertTrue(mockTextLayout.buildLayout(config))
        verify(mockTextLayout).configure(config)
    }
}
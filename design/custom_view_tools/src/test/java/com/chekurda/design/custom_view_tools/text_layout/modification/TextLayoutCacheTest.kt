package com.chekurda.design.custom_view_tools.text_layout.modification

import android.os.Build
import android.text.StaticLayout
import com.chekurda.design.custom_view_tools.TextLayout
import com.chekurda.design.custom_view_tools.TextLayout.Companion.createTextLayoutByStyle
import com.chekurda.design.custom_view_tools.styles.StyleParams
import com.chekurda.design.custom_view_tools.styles.StyleParamsProvider
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import com.chekurda.design.custom_view_tools.text_layout.assertCacheIsEmpty
import com.chekurda.design.custom_view_tools.text_layout.assertCacheIsNotEmpty
import com.nhaarman.mockitokotlin2.doReturn
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Тесты [TextLayout] на предмет ленивого создания [StaticLayout] в поле [TextLayout.cachedLayout].
 *
 * @author vv.chekurda
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.P])
class TextLayoutCacheTest {

    private lateinit var textLayout: TextLayout

    @Before
    fun setUp() {
        textLayout = TextLayout { text = "Test text" }
    }

    // region Don't create StaticLayout

    @Test
    fun `TextLayout don't create StaticLayout after initialize by constructor`() {
        assertNull(textLayout.state.cachedLayout)
    }

    @Test
    fun `TextLayout don't create StaticLayout after initialize by style`() {
        val styleKey = StyleParams.StyleKey(1)
        val textStyle = StyleParams.TextStyle(styleKey)
        val mockStylesProvider = mock<StyleParamsProvider<StyleParams.TextStyle>> {
            on { getStyleParams(any(), any<StyleParams.StyleKey>()) } doReturn textStyle
        }

        val layout = createTextLayoutByStyle(mock(), styleKey, mockStylesProvider)

        assertNull(layout.state.cachedLayout)
    }

    @Test
    fun `TextLayout don't create StaticLayout after call configureLayout()`() {
        assertCacheIsEmpty(textLayout) { configure { text = "123" } }
    }

    @Test
    fun `TextLayout don't create StaticLayout after call buildLayout(), when isVisible isEquals false`() {
        assertCacheIsEmpty(TextLayout { isVisible = false }) { buildLayout { text = "123" } }
    }

    @Test
    fun `TextLayout don't create StaticLayout after second call buildLayout(), when params isn't changed`() {
        textLayout.buildLayout()
        val cacheAfterFirstBuild = textLayout.state.cachedLayout

        val isChanged = textLayout.buildLayout()
        val cachedLayout = textLayout.state.cachedLayout

        assertEquals(cacheAfterFirstBuild, cachedLayout)
        assertFalse(isChanged)
        assertNotNull(cachedLayout)
    }

    @Test
    fun `TextLayout don't create StaticLayout after second call layout(), when params isn't changed`() {
        textLayout.layout(0, 0)
        val cacheAfterFirstBuild = textLayout.state.cachedLayout

        val isChanged = textLayout.buildLayout()
        val cachedLayout = textLayout.state.cachedLayout

        assertEquals(cacheAfterFirstBuild, cachedLayout)
        assertFalse(isChanged)
        assertNotNull(cachedLayout)
    }

    @Test
    fun `TextLayout don't create StaticLayout after second request width`() {
        textLayout.width
        val cacheAfterFirstBuild = textLayout.state.cachedLayout

        textLayout.width
        val cachedLayout = textLayout.state.cachedLayout

        assertEquals(cacheAfterFirstBuild, cachedLayout)
        assertNotNull(cachedLayout)
    }

    @Test
    fun `TextLayout don't create StaticLayout after second request height`() {
        textLayout.height
        val cacheAfterFirstBuild = textLayout.state.cachedLayout

        textLayout.height
        val cachedLayout = textLayout.state.cachedLayout

        assertEquals(cacheAfterFirstBuild, cachedLayout)
        assertNotNull(cachedLayout)
    }

    @Test
    fun `TextLayout don't create StaticLayout after call updatePadding`() {
        assertCacheIsEmpty(textLayout) { updatePadding(1, 2, 3, 4) }
    }

    @Test
    fun `TextLayout don't create StaticLayout after call layout, when isVisible is equals false`() {
        assertCacheIsEmpty(TextLayout { isVisible = false }) { layout(0, 0) }
    }

    @Test
    fun `TextLayout don't create StaticLayout after call draw`() {
        assertCacheIsEmpty(textLayout) { draw(mock()) }
    }

    @Test
    fun `TextLayout don't create StaticLayout after request text`() {
        assertCacheIsEmpty(textLayout) { text }
    }

    @Test
    fun `TextLayout don't create StaticLayout after request textPaint`() {
        assertCacheIsEmpty(textLayout) { textPaint }
    }

    @Test
    fun `TextLayout don't create StaticLayout after request isVisible`() {
        assertCacheIsEmpty(textLayout) { isVisible }
    }

    @Test
    fun `TextLayout don't create StaticLayout after request maxLines`() {
        assertCacheIsEmpty(textLayout) { maxLines }
    }

    @Test
    fun `TextLayout don't create StaticLayout after request left`() {
        assertCacheIsEmpty(textLayout) { left }
    }

    @Test
    fun `TextLayout don't create StaticLayout after request top`() {
        assertCacheIsEmpty(textLayout) { top }
    }

    @Test
    fun `TextLayout don't create StaticLayout after request right`() {
        assertCacheIsEmpty(textLayout) { right }
    }

    @Test
    fun `TextLayout don't create StaticLayout after request bottom`() {
        assertCacheIsEmpty(textLayout) { bottom }
    }

    @Test
    fun `TextLayout don't create StaticLayout after request paddingStart`() {
        assertCacheIsEmpty(textLayout) { paddingStart }
    }

    @Test
    fun `TextLayout don't create StaticLayout after request paddingTop`() {
        assertCacheIsEmpty(textLayout) { paddingTop }
    }

    @Test
    fun `TextLayout don't create StaticLayout after request paddingEnd`() {
        assertCacheIsEmpty(textLayout) { paddingEnd }
    }

    @Test
    fun `TextLayout don't create StaticLayout after request paddingBottom`() {
        assertCacheIsEmpty(textLayout) { paddingBottom }
    }

    @Test
    fun `TextLayout don't create StaticLayout after request getDesiredWidth()`() {
        assertCacheIsEmpty(textLayout) { getDesiredWidth("Test text") }
    }
    @Test
    fun `TextLayout don't create StaticLayout after request width, when isVisible is equals false`() {
        assertCacheIsEmpty(TextLayout { isVisible = false }) { width }
    }

    @Test
    fun `TextLayout don't create StaticLayout after request height, when isVisible is equals false`() {
        assertCacheIsEmpty(TextLayout { isVisible = false }) { height }
    }

    @Test
    fun `TextLayout don't create StaticLayout after request height, when layoutWidth is equals 0`() {
        assertCacheIsEmpty(TextLayout { layoutWidth = 0 }) { height }
    }

    // endregion

    // region Create StaticLayout

    @Test
    fun `TextLayout create StaticLayout after request width`() {
        assertCacheIsNotEmpty(textLayout) { width }
    }

    @Test
    fun `TextLayout create StaticLayout after request height`() {
        assertCacheIsNotEmpty(textLayout) { height }
    }

    @Test
    fun `TextLayout create StaticLayout after request lineCount`() {
        assertCacheIsNotEmpty(textLayout) { lineCount }
    }

    @Test
    fun `TextLayout create StaticLayout after request baseline`() {
        assertCacheIsNotEmpty(textLayout) { baseline }
    }

    @Test
    fun `TextLayout create StaticLayout after call buildLayout(), when isVisible is equals true`() {
        assertCacheIsNotEmpty(textLayout) { buildLayout() }
        assertTrue(textLayout.isVisible)
    }

    @Test
    fun `TextLayout create StaticLayout after second call buildLayout(), when params is changed`() {
        textLayout.buildLayout()
        val cacheAfterFirstBuild = textLayout.state.cachedLayout

        val isChanged = textLayout.buildLayout { text = "123456" }

        assertNotEquals(cacheAfterFirstBuild, textLayout.state.cachedLayout)
        assertTrue(isChanged)
    }

    @Test
    fun `TextLayout create StaticLayout after call layout(), when isVisible is equals true`() {
        assertCacheIsNotEmpty(textLayout) { layout(0, 0) }
        assertTrue(textLayout.isVisible)
    }

    // endregion
}
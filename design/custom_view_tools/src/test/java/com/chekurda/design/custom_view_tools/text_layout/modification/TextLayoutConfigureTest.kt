package com.chekurda.design.custom_view_tools.text_layout.modification

import android.graphics.Color
import android.os.Build
import android.text.TextUtils.TruncateAt
import com.chekurda.design.custom_view_tools.TextLayout
import com.chekurda.design.custom_view_tools.TextLayout.TextLayoutPadding
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import com.chekurda.design.custom_view_tools.utils.TextHighlights
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Тесты метода [TextLayout.configure] для модификации параметров [TextLayout.params].
 *
 * @author vv.chekurda
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.P])
class TextLayoutConfigureTest {

    private lateinit var textLayout: TextLayout

    @Before
    fun setUp() {
        textLayout = TextLayout { text = "Test text" }
            .apply { buildLayout() }
    }

    @Test
    fun `When call configure() and params isn't changed, then don't mark layout as changed and return false`() {
        val params = textLayout.state.params

        val isChanged = textLayout.configure {
            text = params.text
            paint.textSize = params.paint.textSize
            paint.color = params.paint.color
            layoutWidth = params.layoutWidth
            alignment = params.alignment
            ellipsize = params.ellipsize
            includeFontPad = params.includeFontPad
            maxLines = params.maxLines
            padding = params.padding
            isVisible = params.isVisible
            highlights = params.highlights
            minWidth = params.minWidth
            minHeight = params.minHeight
            maxWidth = params.maxWidth
            maxHeight = params.maxHeight
            isVisibleWhenBlank = params.isVisibleWhenBlank
            canContainUrl = params.canContainUrl
        }

        assertFalse(isChanged)
        assertFalse(textLayout.state.isLayoutChanged)
    }

    @Test
    fun `When call configure() and textColor is changed, then don't mark layout as changed and return false`() {
        val oldTextColor = textLayout.state.params.paint.color
        val newTextColor = -1

        val isChanged = textLayout.configure { paint.color = newTextColor }

        assertFalse(isChanged)
        assertFalse(textLayout.state.isLayoutChanged)
        assertNotEquals(oldTextColor, newTextColor)
    }

    @Test
    fun `When call configure() and textSize is changed, then mark layout as changed and return true`() {
        val oldTextSize = textLayout.state.params.paint.textSize
        val newTextSize = 200f

        assertTrue(textLayout.configure { paint.textSize = newTextSize })
        assertTrue(textLayout.state.isLayoutChanged)
        assertNotEquals(oldTextSize, newTextSize)
    }

    @Test
    fun `When call configure() and text is changed, then mark layout as changed and return true`() {
        val oldText = textLayout.state.params.text
        val newText = "TEST TEXT"

        assertTrue(textLayout.configure { text = newText })
        assertTrue(textLayout.state.isLayoutChanged)
        assertNotEquals(oldText, newText)
    }

    @Test
    fun `When call configure() and layoutWidth is changed, then mark layout as changed and return true`() {
        val oldLayoutWidth = textLayout.state.params.layoutWidth
        val newLayoutWidth = 200

        assertTrue(textLayout.configure { layoutWidth = newLayoutWidth })
        assertTrue(textLayout.state.isLayoutChanged)
        assertNotEquals(oldLayoutWidth, newLayoutWidth)
    }

    @Test
    fun `When call configure() and ellipsize is changed, then mark layout as changed and return true`() {
        val oldEllipsize = textLayout.state.params.ellipsize
        val newEllipsize = TruncateAt.START

        assertTrue(textLayout.configure { ellipsize = newEllipsize })
        assertTrue(textLayout.state.isLayoutChanged)
        assertNotEquals(oldEllipsize, newEllipsize)
    }

    @Test
    fun `When call configure() and maxLines is changed, then mark layout as changed and return true`() {
        val oldMaxLines = textLayout.state.params.maxLines
        val newMaxLines = 50

        assertTrue(textLayout.configure { maxLines = newMaxLines })
        assertTrue(textLayout.state.isLayoutChanged)
        assertNotEquals(oldMaxLines, newMaxLines)
    }

    @Test
    fun `When call configure() and isVisible is changed, then mark layout as changed and return true`() {
        val oldIsVisible = textLayout.state.params.isVisible
        val newIsVisible = false

        assertTrue(textLayout.configure { isVisible = newIsVisible })
        assertTrue(textLayout.state.isLayoutChanged)
        assertNotEquals(oldIsVisible, newIsVisible)
    }

    @Test
    fun `When call configure() and padding is changed, then mark layout as changed and return true`() {
        val oldPadding = textLayout.state.params.padding
        val newPadding = TextLayoutPadding(1, 2, 3, 4)

        assertTrue(textLayout.configure { padding = newPadding })
        assertTrue(textLayout.state.isLayoutChanged)
        assertNotEquals(oldPadding, newPadding)
    }

    @Test
    fun `When call configure() and highlights is changed, then mark layout as changed and return true`() {
        val oldHighlights = textLayout.state.params.highlights
        val newHighlights = TextHighlights(listOf(), Color.YELLOW)

        assertTrue(textLayout.configure { highlights = newHighlights })
        assertTrue(textLayout.state.isLayoutChanged)
        assertNotEquals(oldHighlights, newHighlights)
    }

    @Test
    fun `When call configure() and minWidth is changed, then mark layout as changed and return true`() {
        val oldMinHeight = textLayout.state.params.minWidth
        val newMinHeight = 50

        assertTrue(textLayout.configure { minHeight = newMinHeight })
        assertTrue(textLayout.state.isLayoutChanged)
        assertNotEquals(oldMinHeight, newMinHeight)
    }

    @Test
    fun `When call configure() and minHeight is changed, then mark layout as changed and return true`() {
        val oldMinHeight = textLayout.state.params.minHeight
        val newMinHeight = 50

        assertTrue(textLayout.configure { minHeight = newMinHeight })
        assertTrue(textLayout.state.isLayoutChanged)
        assertNotEquals(oldMinHeight, newMinHeight)
    }

    @Test
    fun `When call configure() and maxWidth is changed, then mark layout as changed and return true`() {
        val oldMaxWidth = textLayout.state.params.maxWidth
        val newMaxWidth = 50

        assertTrue(textLayout.configure { maxWidth = newMaxWidth })
        assertTrue(textLayout.state.isLayoutChanged)
        assertNotEquals(oldMaxWidth, newMaxWidth)
    }

    @Test
    fun `When call configure() and maxHeight is changed, then mark layout as changed and return true`() {
        val oldMaxHeight = textLayout.state.params.maxHeight
        val newMaxHeight = 50

        assertTrue(textLayout.configure { maxHeight = newMaxHeight })
        assertTrue(textLayout.state.isLayoutChanged)
        assertNotEquals(oldMaxHeight, newMaxHeight)
    }
}
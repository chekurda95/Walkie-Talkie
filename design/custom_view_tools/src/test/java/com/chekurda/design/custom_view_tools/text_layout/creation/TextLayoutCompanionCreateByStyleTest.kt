package com.chekurda.design.custom_view_tools.text_layout.creation

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.text.Layout.Alignment
import android.text.TextPaint
import android.text.TextUtils.TruncateAt
import androidx.annotation.StyleRes
import com.chekurda.design.custom_view_tools.TextLayout
import com.chekurda.design.custom_view_tools.TextLayout.Companion.createTextLayoutByStyle
import com.chekurda.design.custom_view_tools.TextLayout.TextLayoutParams
import com.chekurda.design.custom_view_tools.styles.StyleParams.*
import com.chekurda.design.custom_view_tools.styles.StyleParamsProvider
import com.chekurda.design.custom_view_tools.utils.TextHighlights
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.same
import com.nhaarman.mockitokotlin2.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import org.junit.runner.RunWith
import com.chekurda.design.custom_view_tools.text_layout.assertNotEqualsExcludingPaint
import com.chekurda.design.custom_view_tools.text_layout.assertParamsEquals
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Тесты создания [TextLayout] с помощью стиля через [TextLayout.createTextLayoutByStyle].
 *
 * @author vv.chekurda
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.P])
class TextLayoutCompanionCreateByStyleTest {

    private val mockContext = mock<Context>()
    private val defaultParams = TextLayoutParams()
    @StyleRes private val validStyleRes = 1543425434

    @Test
    fun `When styleRes isn't valid and equals 0, then TextLayout has default params`() {
        @StyleRes val styleRes = 0

        val layout = createTextLayoutByStyle(mockContext, styleRes)

        assertParamsEquals(defaultParams, layout.state.params)
    }

    @Test
    fun `When styleRes is valid, then TextLayout try obtain style`() {
        val styleKey = StyleKey(validStyleRes)
        val textStyle = TextStyle(styleKey)
        val mockStylesProvider = mock<StyleParamsProvider<TextStyle>> {
            on { getStyleParams(any(), eq(styleKey)) } doReturn textStyle
        }

        createTextLayoutByStyle(mockContext, validStyleRes, mockStylesProvider)

        verify(mockStylesProvider).getStyleParams(any(), eq(styleKey))
        assertNotEquals(0, styleKey.styleRes)
    }

    @Test
    fun `When styleKey is valid, then TextLayout try obtain style`() {
        val styleKey = StyleKey(validStyleRes)
        val textStyle = TextStyle(styleKey)
        val mockStylesProvider = mock<StyleParamsProvider<TextStyle>> {
            on { getStyleParams(any(), any<StyleKey>()) } doReturn textStyle
        }

        createTextLayoutByStyle(mockContext, styleKey, mockStylesProvider)

        verify(mockStylesProvider).getStyleParams(any(), same(styleKey))
        assertNotEquals(0, styleKey.styleRes)
    }

    @Test
    fun `When styleKey is valid, then TextLayout has styled params`() {
        val styleKey = StyleKey(validStyleRes)
        val textStyle = mockTextStyle(styleKey)
        val mockStylesProvider = mock<StyleParamsProvider<TextStyle>> {
            on { getStyleParams(any(), any<StyleKey>()) } doReturn textStyle
        }

        val layout = createTextLayoutByStyle(mockContext, styleKey, mockStylesProvider)
        val resultParams = layout.state.params

        assertEquals(textStyle.text, resultParams.text)
        assertEquals(textStyle.textSize, resultParams.paint.textSize)
        assertEquals(textStyle.textColor, resultParams.paint.color)
        assertEquals(textStyle.layoutWidth, resultParams.layoutWidth)
        assertEquals(textStyle.alignment, resultParams.alignment)
        assertEquals(textStyle.ellipsize, resultParams.ellipsize)
        assertEquals(textStyle.includeFontPad, resultParams.includeFontPad)
        assertEquals(textStyle.maxLines, resultParams.maxLines)
        assertEquals(textStyle.paddingStyle?.paddingStart, resultParams.padding.start)
        assertEquals(textStyle.paddingStyle?.paddingTop, resultParams.padding.top)
        assertEquals(textStyle.paddingStyle?.paddingEnd, resultParams.padding.end)
        assertEquals(textStyle.paddingStyle?.paddingBottom, resultParams.padding.bottom)
        assertEquals(textStyle.isVisible, resultParams.isVisible)
        assertNotEqualsExcludingPaint(defaultParams, resultParams)
    }

    @Test
    fun `When obtainPadding is equals false, then TextLayout don't has padding from style`() {
        val styleKey = StyleKey(validStyleRes)
        val textStyle = TextStyle(
            styleKey,
            paddingStyle = PaddingStyle(styleKey, 1, 2, 3, 4)
        )
        val mockStylesProvider = mock<StyleParamsProvider<TextStyle>> {
            on { getStyleParams(any(), any<StyleKey>()) } doReturn textStyle
        }

        val layout = createTextLayoutByStyle(mockContext, styleKey, mockStylesProvider, obtainPadding = false)

        assertEquals(defaultParams.padding, layout.state.params.padding)
    }

    @Test
    fun `TextLayout has styled only those parameters that were contained in the style`() {
        val styleKey = StyleKey(validStyleRes)
        val textStyle = TextStyle(
            styleKey,
            text = "Test string",
            textSize = 255f,
            textColor = 244,
            layoutWidth = 400
        )
        val mockStylesProvider = mock<StyleParamsProvider<TextStyle>> {
            on { getStyleParams(any(), any<StyleKey>()) } doReturn textStyle
        }

        val layout = createTextLayoutByStyle(mockContext, styleKey, mockStylesProvider)
        val resultParams = layout.state.params

        assertEquals(textStyle.text, resultParams.text)
        assertEquals(textStyle.textSize, resultParams.paint.textSize)
        assertEquals(textStyle.textColor, resultParams.paint.color)
        assertEquals(textStyle.layoutWidth, resultParams.layoutWidth)

        assertEquals(defaultParams.alignment, resultParams.alignment)
        assertEquals(defaultParams.ellipsize, resultParams.ellipsize)
        assertEquals(defaultParams.maxLines, resultParams.maxLines)
        assertEquals(defaultParams.padding, resultParams.padding)
        assertEquals(defaultParams.isVisible, resultParams.isVisible)
        assertEquals(defaultParams.highlights, resultParams.highlights)
        assertEquals(defaultParams.minWidth, resultParams.minWidth)
        assertEquals(defaultParams.minHeight, resultParams.minHeight)
        assertEquals(defaultParams.maxWidth, resultParams.maxWidth)
        assertEquals(defaultParams.maxHeight, resultParams.maxHeight)
        assertEquals(defaultParams.isVisibleWhenBlank, resultParams.isVisibleWhenBlank)
        assertEquals(defaultParams.canContainUrl, resultParams.canContainUrl)
        assertNotEqualsExcludingPaint(defaultParams, resultParams)
    }

    @Test
    fun `PostConfig modification will be applied after applying style parameters`() {
        val styleKey = StyleKey(validStyleRes)
        val textStyle = mockTextStyle(styleKey)
        val mockStylesProvider = mock<StyleParamsProvider<TextStyle>> {
            on { getStyleParams(any(), any<StyleKey>()) } doReturn textStyle
        }
        val mockParams = mockTextLayoutParams()

        val layout = createTextLayoutByStyle(mockContext, styleKey, mockStylesProvider) {
            text = mockParams.text
            paint.textSize = mockParams.paint.textSize
            paint.color = mockParams.paint.color
            layoutWidth = mockParams.layoutWidth
            alignment = mockParams.alignment
            ellipsize = mockParams.ellipsize
            includeFontPad = mockParams.includeFontPad
            maxLines = mockParams.maxLines
            padding = mockParams.padding
            isVisible = mockParams.isVisible
            highlights = mockParams.highlights
            minWidth = mockParams.minWidth
            minHeight = mockParams.minHeight
            maxWidth = mockParams.maxWidth
            maxHeight = mockParams.maxHeight
            isVisibleWhenBlank = mockParams.isVisibleWhenBlank
            canContainUrl = mockParams.canContainUrl
        }
        val resultParams = layout.state.params

        assertParamsEquals(mockParams, resultParams)
        assertNotEqualsExcludingPaint(defaultParams, resultParams)
    }

    private fun mockTextStyle(styleKey: StyleKey) =
        TextStyle(
            styleKey,
            text = "Test string",
            textSize = 255f,
            textColor = 244,
            layoutWidth = 400,
            alignment = Alignment.ALIGN_OPPOSITE,
            ellipsize = TruncateAt.MARQUEE,
            includeFontPad = false,
            maxLines = 200,
            paddingStyle = PaddingStyle(styleKey, 1, 2, 3 , 4),
            isVisible = false
        )

    private fun mockTextLayoutParams() =
        TextLayoutParams(
            text = "Test params string",
            paint = TextPaint().apply {
                textSize = 1f
                color = 2
            },
            layoutWidth = 3,
            alignment = Alignment.ALIGN_CENTER,
            ellipsize = TruncateAt.MIDDLE,
            includeFontPad = true,
            maxLines = 200,
            padding = TextLayout.TextLayoutPadding(4, 5, 6, 7),
            isVisible = false,
            highlights = TextHighlights(listOf(), Color.YELLOW),
            minWidth = 0,
            minHeight = 0,
            maxWidth = 200,
            maxHeight = 400,
            isVisibleWhenBlank = false,
            canContainUrl = true
        )
}
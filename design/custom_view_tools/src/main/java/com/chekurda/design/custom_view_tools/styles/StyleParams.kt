package com.chekurda.design.custom_view_tools.styles

import android.text.Layout
import android.text.TextUtils.TruncateAt
import androidx.annotation.ColorInt
import androidx.annotation.Px
import androidx.annotation.StyleRes
import androidx.core.content.res.ResourcesCompat.ID_NULL
import org.apache.commons.lang3.StringUtils.EMPTY

/**
 * Параметры стиля.
 *
 * @property styleKey ключ стиля.
 */
sealed class StyleParams(val styleKey: StyleKey) {

    /**
     * Ключ стиля.
     *
     * @property styleRes ресурс стиля.
     * @property tag тег стиля. Атрибуты одного стиля [styleRes] могут зависеть от разных тем,
     * поэтому для кеширования в таких сценариях необходим дополнительный [tag].
     */
    data class StyleKey(@StyleRes val styleRes: Int, val tag: String = EMPTY)

    /**
     * Параметры стиля текста.
     *
     * @property text текст.
     * @property textSize размер текста.
     * @property textColor цвет текста.
     * @property layoutWidth ширина разметки.
     * @property alignment мод выравнивания текста.
     * @property ellipsize мод сокращения текста.
     * @property includeFontPad включить стандартные отступы шрифта.
     * @property maxLines максимальное количество строк.
     * @property paddingStyle модель стиля отступов.
     * @property isVisible состояние видимости.
     */
    class TextStyle(
        styleKey: StyleKey,
        val text: String? = null,
        @Px val textSize: Float? = null,
        @ColorInt val textColor: Int? = null,
        @Px val layoutWidth: Int? = null,
        val alignment: Layout.Alignment? = null,
        val ellipsize: TruncateAt? = null,
        val includeFontPad: Boolean? = null,
        val maxLines: Int? = null,
        val paddingStyle: PaddingStyle? = null,
        val isVisible: Boolean? = null
    ) : StyleParams(styleKey)

    /**
     * Параметры стиля отступов.
     *
     * @property paddingStart левый отступ.
     * @property paddingTop верхний отступ.
     * @property paddingEnd правый отступ.
     * @property paddingBottom нижний отступ.
     */
    class PaddingStyle(
        styleKey: StyleKey = StyleKey(ID_NULL),
        @Px val paddingStart: Int = 0,
        @Px val paddingTop: Int = 0,
        @Px val paddingEnd: Int = 0,
        @Px val paddingBottom: Int = 0
    ) : StyleParams(styleKey)
}

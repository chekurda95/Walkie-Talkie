/**
 * Утилиты для выделения текста.
 */
package com.chekurda.design.custom_view_tools.utils

import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.TextUtils
import android.text.TextUtils.TruncateAt
import android.text.style.BackgroundColorSpan
import androidx.annotation.ColorInt
import androidx.annotation.Px
import org.apache.commons.lang3.StringUtils
import timber.log.Timber

/**
 * Модель с информацией для выделения текста.
 *
 * @property highlightColor цвет выделения.
 * @property positionList список позиций для выделения.
 */
data class TextHighlights(val positionList: List<HighlightSpan>?, @ColorInt val highlightColor: Int)

/**
 * Модель позиций выделения.
 * Левая позиция включительно, правая позиция исключительно.
 */
data class HighlightSpan(val start: Int = 0, val end: Int = 0) {

    override fun toString(): String = "SearchSpan{start=$start, end=$end}"
}

/**
 * Получить строку с выделенным текстом по модели [highlights].
 */
fun CharSequence.highlightText(highlights: TextHighlights?): CharSequence {
    val highlightPositions = highlights?.positionList
    if (isEmpty() || highlightPositions.isNullOrEmpty()) return this

    return (this as? Spannable ?: SpannableString(this)).apply {
        try {
            // Вычисление последней позиции текста до сокращения.
            val lastTextPosition = lastTextIndex
            highlightPositions.forEach {
                when {
                    // Находимся в границах текста до сокращения -> выделяем текст.
                    it.start <= lastTextPosition && it.end <= lastTextPosition + 1 ->
                        setHighlightSpan(highlights.highlightColor, it.start, it.end)

                    // Вышли за границы текста до сокращения -> выделяем сокращение.
                    lastTextPosition < length -> {
                        val start = minOf(it.start, lastTextPosition + 1)
                        setHighlightSpan(highlights.highlightColor, start, length)
                        return@apply
                    }

                    else -> return@apply
                }
            }
        } catch (ex: IndexOutOfBoundsException) {
            Timber.e("Ошибка позиций выделения текста при поиске: ${ex.message}")
        }
    }
}

/**
 * Обрезать текст и установить выделения [HighlightSpan].
 * @see highlightText
 *
 * @param paint краска текста.
 * @param width ширина текста.
 * @param highlights модель для выделения текста.
 * @param ellipsize мод сокращения текста.
 */
fun CharSequence.ellipsizeAndHighlightText(
    paint: TextPaint,
    @Px width: Int,
    highlights: TextHighlights?,
    ellipsize: TruncateAt = TruncateAt.END
): CharSequence =
    if (width > 0 && isNotBlank()) {
        TextUtils.ellipsize(this, paint, width.toFloat(), ellipsize).let { ellipsizedText ->
            if (!highlights?.positionList.isNullOrEmpty()) ellipsizedText.highlightText(highlights)
            else ellipsizedText
        }
    } else {
        StringUtils.EMPTY
    }

/**
 * Получить индекс последнего символа текста до сокращения.
 * @see hasSymbolEllipsize
 * @see hasSimpleEllipsize
 */
internal val CharSequence.lastTextIndex: Int
    get() = when {
        hasSymbolEllipsize -> lastIndex - ELLIPSIZE_CHAR.length
        hasSimpleEllipsize -> lastIndex - ELLIPSIZE_STRING.length
        else -> lastIndex
    }

/**
 * Признак сокращения текста посредством символа троеточия [ELLIPSIZE_CHAR].
 */
internal val CharSequence.hasSymbolEllipsize: Boolean
    get() = length > 0 && last().toString() == ELLIPSIZE_CHAR

/**
 * Признак простого сокращения текста посредством трех символов точек [ELLIPSIZE_STRING].
 */
internal val CharSequence.hasSimpleEllipsize: Boolean
    get() = length > ELLIPSIZE_STRING.length && lastIndexOf(ELLIPSIZE_STRING) == length - ELLIPSIZE_STRING.length

/**
 * Применить span выделения [Spannable] текста от позиции [start] (включительно) до позиции [end] (не включительно),
 * и получить результат.
 */
internal fun Spannable.setHighlightSpan(
    @ColorInt highlightColor: Int,
    start: Int,
    end: Int
): Spannable = apply {
    if (end - start <= 0) return@apply
    setSpan(
        BackgroundColorSpan(highlightColor), start, end,
        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
    )
}

private const val ELLIPSIZE_CHAR = "\u2026"
private const val ELLIPSIZE_STRING = "..."
package com.chekurda.design.custom_view_tools.utils

import android.graphics.Paint
import android.text.TextPaint
import androidx.annotation.Px
import kotlin.math.ceil

/**
 * Обычный anti-alias [Paint] с возможностью настройки прямо в конструкторе.
 * Несколько упрощает синтаксис создания обычного Paint.
 */
class SimplePaint(config: (SimplePaint.() -> Unit)? = null) : Paint(ANTI_ALIAS_FLAG) {
    init {
        config?.invoke(this)
    }
}

/**
 * Обычный anti-alias [TextPaint] с возможностью настройки прямо в конструкторе.
 * Несколько упрощает синтаксис создания обычного TextPaint.
 */
class SimpleTextPaint(config: (SimpleTextPaint.() -> Unit)? = null) : TextPaint(ANTI_ALIAS_FLAG) {
    init {
        config?.invoke(this)
    }
}

const val PAINT_MAX_ALPHA = 255


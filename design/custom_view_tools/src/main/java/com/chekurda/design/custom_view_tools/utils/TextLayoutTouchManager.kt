package com.chekurda.design.custom_view_tools.utils

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IntRange
import com.chekurda.design.custom_view_tools.TextLayout

/**
 * Менеджер для упрощения работы с обработкой касаний по нескольким текстовым разметкам [TextLayout] в кастомной [View].
 *
 * @property view вью, в которой находятся текстовые разметки [TextLayout].
 * @property layoutList список текстовых разметок, участвующих в обработке касаний.
 *
 * Может облегчить жизнь в сценариях использования нескольких [TextLayout] в рамках одной [View],
 * если необходим следующий функционал разметки:
 * - [TextLayout.setOnClickListener]
 * - [TextLayout.setOnLongClickListener]
 * - [TextLayout.colorStateList]
 *
 * Менеджер включает кликабельность текстовых разметок из списка [layoutList],
 * а также делегирует события касаний между [TextLayout] с соблюдением приоритетов порядка в списке.
 *
 * Логика делегирования [MotionEvent] в [TextLayout] аналогична обработке событий касания во [ViewGroup] -
 * если в позиции касания границы разметок из [layoutList] пересекаются,
 * то приоритет обработки касания будет иметь тот [TextLayout],
 * который находится ближе к концу списка, т.е. условно находится поверх других [TextLayout].
 *
 * Способ подключения:
 * ```
 * class ExampleCustomView(context: Context) : View(context) {
 *
 *     private val textLayout1 = TextLayout()
 *     private val textLayout2 = TextLayout()
 *
 *     private val touchManager = TextLayoutTouchManager(textLayout1, textLayout2)
 *
 *     override fun onTouchEvent(event: MotionEvent): Boolean =
 *         touchManager.onTouch(this, event) || super.onTouchEvent(event)
 * }
 * ```
 *
 * @author vv.chekurda
 */
class TextLayoutTouchManager(
    private val view: View,
    private val layoutList: MutableList<TextLayout> = mutableListOf()
) : View.OnTouchListener {

    constructor(
        view: View,
        vararg layouts: TextLayout
    ) : this(view, layouts.toMutableList())

    init {
        makeLayoutsClickable()
    }

    /**
     * Список текстовых разметок менеджера.
     */
    val layouts: List<TextLayout>
        get() = layoutList

    /**
     * Текстовая разметка, которая последняя обрабатывала событие касания.
     */
    private var lastTouchedLayout: TextLayout? = null

    /**
     * Добавить тестовую разметку [layout] в менеджер на позицию [index].
     *
     * [index] по-умолчанию помещает [layout] на вершину обработки касаний [layoutList].
     */
    fun add(
        layout: TextLayout,
        @IntRange(from = -1) index: Int = -1
    ) {
        layoutList.apply {
            layout.makeClickable(view)
            remove(layout)
            add(if (index < 0) size else index, layout)
        }
    }

    /**
     * Добавить список тестовых разметок [layouts] в менеджер с позиции [index].
     *
     * [index] по-умолчанию помещает [layouts] на вершину обработки касаний [layoutList].
     */
    fun addAll(
        layouts: List<TextLayout>,
        @IntRange(from = -1) index: Int = -1
    ) {
        layoutList.apply {
            removeAll(layouts)
            addAll(if (index < 0) size else index, layouts)
            makeLayoutsClickable()
        }
    }

    /**
     * Добавить перечень тестовых разметок [layouts] в менеджер с позиции [index].
     *
     * [index] по-умолчанию помещает [layouts] на вершину обработки касаний [layoutList].
     */
    fun addAll(
        vararg layouts: TextLayout,
        @IntRange(from = -1) index: Int = -1
    ) {
        layoutList.apply {
            removeAll(layouts)
            addAll(if (index < 0) size else index, layouts.toList())
            makeLayoutsClickable()
        }
    }

    /**
     * Удалить текстовую разметку [layout] из менеджера.
     */
    fun remove(layout: TextLayout) {
        layoutList.remove(layout)
    }

    /**
     * Очистить менеджер от обрабатываемых текстовых разметок.
     */
    fun clear() {
        layoutList.clear()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View, event: MotionEvent): Boolean =
        layoutList.findLast { it.onTouch(v, event) }
            .also { touchedLayout ->
                lastTouchedLayout?.takeIf { it !== touchedLayout }
                    ?.onTouchCanceled()
                lastTouchedLayout = touchedLayout
            } != null

    private fun makeLayoutsClickable() {
        layoutList.forEach { it.makeClickable(view) }
    }
}
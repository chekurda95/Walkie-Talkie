package com.chekurda.design.custom_view_tools.utils

import android.annotation.SuppressLint
import android.view.View
import android.view.View.AccessibilityDelegate
import android.view.accessibility.AccessibilityNodeInfo
import androidx.core.content.res.ResourcesCompat.ID_NULL
import com.chekurda.design.custom_view_tools.TextLayout
import org.json.JSONArray
import org.json.JSONObject

/**
 * Вспомогательный класс для автотестов кастомных [View], использующих компонент текстовой разметки [TextLayout].
 *
 * @param view вью, в которой находятся текстовые разметки [TextLayout].
 * @property layoutSet набор текстовых разметок, которые использует view.
 *
 * [TextLayout] не является [View] компонентом и не отображается в иерархии [View], например, в Layout Inspector.
 * Данный класс позволяет при включенном режиме "специальных возможностей" передавать полезную информацию состояния
 * текстовых разметок из списка [layoutSet] в системные вызовы [AccessibilityDelegate.onInitializeAccessibilityNodeInfo]
 * в качестве JSON текста в [AccessibilityNodeInfo], которая доступна для автотестирования,
 * как дополнительная информация о [View].
 *
 * Для каждого [TextLayout] в JSON передаются следующие параметры:
 * - [TextLayout.id] идентификатор
 * - [TextLayout.text] текст
 * - [TextLayout.textPaint] цвет и размер текста из краски
 * - [TextLayout.width] ширина
 * - [TextLayout.height] высота
 * - [TextLayout.left] левая координата относительно [View]
 * - [TextLayout.top] верхняя координата относительно [View]
 * - [TextLayout.right] правая координата относительно [View]
 * - [TextLayout.bottom] нижняя координата относительно [View]
 * - [TextLayout.isVisible] признак видимости
 * - [TextLayout.lineCount] количество строк
 * - [TextLayout.maxLines] максимально допустимое количество строк
 * - [TextLayout.paddingStart] левый отступ
 * - [TextLayout.paddingTop] верхний отступ
 * - [TextLayout.paddingEnd] правый отступ
 * - [TextLayout.paddingBottom] нижний отступ
 * - [TextLayout.isEnabled] состояния доступности
 * - [TextLayout.isPressed] состояние нажатости
 * - [TextLayout.isSelected] состояние выбранности
 *
 * Способ подключения:
 * ```
 * class ExampleCustomView(context: Context) : View(context) {
 *
 *     private val textLayout1 = TextLayout()
 *     private val textLayout2 = TextLayout()
 *     // Для примера опционального поля
 *     private var textLayout3: TextLayout? = null
 *
 *     private val autoTestsHelper = TextLayoutAutoTestsHelper(textLayout1, textLayout2)
 *
 *     // Для примера nullable разметок:
 *     fun setData(data: Data) {
 *         textLayout3 = if (data.needAddLayout) {
 *              // Если по какой-то причине существует необходимость отложенного создания TextLayout,
 *              // то новые разметки можно добавить через методы add/addAll.
 *              TextLayout().also(autoTestsHelper::add)
 *         } else {
 *              // При необходимости удалить старые разметки можно через методы remove/clear.
 *              textLayout3?.let(autoTestsHelper::remove)
 *              null
 *         }
 *     }
 * }
 * ```
 *
 * @author vv.chekurda
 */
class TextLayoutAutoTestsHelper(
    view: View,
    private val layoutSet: MutableSet<TextLayout> = mutableSetOf()
) : AccessibilityDelegate() {

    constructor(view: View, vararg layouts: TextLayout) : this(view, layouts.toMutableSet())

    init {
        view.accessibilityDelegate = this
    }

    /**
     * Список текстовых разметок хелпера, информация по которым собирается [AccessibilityDelegate].
     */
    val layouts: List<TextLayout>
        get() = layoutSet.toList()

    /**
     * Добавить в обработку тестовую разметку [layout].
     */
    fun add(layout: TextLayout) {
        layoutSet.add(layout)
    }

    /**
     * Добавить в обработку список тестовых разметок [layouts].
     */
    fun addAll(layouts: List<TextLayout>) {
        layoutSet.addAll(layouts)
    }

    /**
     * Добавить в обработку перечень тестовых разметок [layouts].
     */
    fun addAll(vararg layouts: TextLayout) {
        layoutSet.addAll(layouts.toList())
    }

    /**
     * Удалить из обработки текстовую разметку [layout].
     */
    fun remove(layout: TextLayout) {
        layoutSet.remove(layout)
    }

    /**
     * Очистить список обрабатываемых текстовых разметок.
     */
    fun clear() {
        layoutSet.clear()
    }

    @SuppressLint("DefaultLocale")
    override fun onInitializeAccessibilityNodeInfo(host: View?, info: AccessibilityNodeInfo?) {
        super.onInitializeAccessibilityNodeInfo(host, info)
        if (host == null || info == null) return

        val jsonArray = JSONArray()
        val accessibilityDescription = StringBuilder()
        layoutSet.forEach { layout ->
            val id = layout.id.takeIf { it != ID_NULL }
                ?.let { host.resources.getResourceEntryName(it) }
                ?: NO_ID_VALUE
            val color = String.format(
                COLOR_HEX_STRING_FORMAT,
                layout.textPaint.color and 0xFFFFFF
            ).toUpperCase()
            accessibilityDescription.appendLine(layout.text)

            JSONObject().apply {
                put(ID_KEY, id)
                put(TEXT_KEY, layout.text)
                put(TEXT_SIZE_KEY, layout.textPaint.textSize)
                put(TEXT_COLOR_KEY, color)
                put(WIDTH_KEY, layout.width)
                put(HEIGHT_KEY, layout.height)
                put(LEFT_KEY, layout.left)
                put(TOP_KEY, layout.top)
                put(RIGHT_KEY, layout.right)
                put(BOTTOM_KEY, layout.bottom)
                put(IS_VISIBLE_KEY, layout.isVisible)
                put(LINE_COUNT_KEY, layout.lineCount)
                put(MAX_LINES_KEY, layout.maxLines)
                put(PADDING_START_KEY, layout.paddingStart)
                put(PADDING_TOP_KEY, layout.paddingTop)
                put(PADDING_END_KEY, layout.paddingEnd)
                put(PADDING_BOTTOM_KEY, layout.paddingBottom)
                put(IS_ENABLED_KEY, layout.isEnabled)
                put(IS_PRESSED_KEY, layout.isPressed)
                put(IS_SELECTED_KEY, layout.isSelected)
            }.let(jsonArray::put)
        }
        info.run {
            contentDescription = accessibilityDescription
            text = jsonArray.toString()
        }
    }
}

/** Ключ идентификатора разметки */
private const val ID_KEY = "id"
/** Ключ текста разметки */
private const val TEXT_KEY = "text"
/** Ключ размера текста разметки */
private const val TEXT_SIZE_KEY = "textSize"
/** Ключ цвета текста разметки */
private const val TEXT_COLOR_KEY = "textColor"
/** Ключ ширини разметки */
private const val WIDTH_KEY = "width"
/** Ключ высоты разметки */
private const val HEIGHT_KEY = "height"
/** Ключ левой позиции разметки относительно [View] */
private const val LEFT_KEY = "left"
/** Ключ верхней позиции разметки относительно [View] */
private const val TOP_KEY = "top"
/** Ключ правой позиции разметки относительно [View] */
private const val RIGHT_KEY = "right"
/** Ключ нижней позиции разметки относительно [View] */
private const val BOTTOM_KEY = "bottom"
/** Ключ признака видимости разметки */
private const val IS_VISIBLE_KEY = "isVisible"
/** Ключ количества строк разметки */
private const val LINE_COUNT_KEY = "lineCount"
/** Ключ максимально допустимого количества строк разметки */
private const val MAX_LINES_KEY = "maxLines"
/** Ключ левого отступа разметки */
private const val PADDING_START_KEY = "paddingStart"
/** Ключ верхнего отсутпа разметки */
private const val PADDING_TOP_KEY = "paddingTop"
/** Ключ правого отсутпа разметки */
private const val PADDING_END_KEY = "paddingEnd"
/** Ключ нижнего отсутпа разметки */
private const val PADDING_BOTTOM_KEY = "paddingBottom"
/** Ключ состояния доступности разметки */
private const val IS_ENABLED_KEY = "isEnabled"
/** Ключ состояния нажатости разметки */
private const val IS_PRESSED_KEY = "isPressed"
/** Ключ состояния выбранности разметки */
private const val IS_SELECTED_KEY = "isSelected"
/** Значение пустого идентификатора **/
private const val NO_ID_VALUE ="no_id"
private const val COLOR_HEX_STRING_FORMAT = "#%06x"
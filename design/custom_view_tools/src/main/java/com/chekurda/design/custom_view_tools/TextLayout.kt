package com.chekurda.design.custom_view_tools

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.text.Layout
import android.text.Layout.Alignment
import android.text.StaticLayout
import android.text.TextPaint
import android.text.TextUtils.TruncateAt
import android.view.*
import androidx.annotation.AttrRes
import androidx.annotation.IdRes
import androidx.annotation.Px
import androidx.annotation.StyleRes
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.withTranslation
import org.apache.commons.lang3.StringUtils
import com.chekurda.design.custom_view_tools.TextLayout.Companion.createTextLayoutByStyle
import com.chekurda.design.custom_view_tools.TextLayout.TextLayoutParams
import com.chekurda.design.custom_view_tools.styles.CanvasStylesProvider
import com.chekurda.design.custom_view_tools.styles.StyleParams.StyleKey
import com.chekurda.design.custom_view_tools.styles.StyleParams.TextStyle
import com.chekurda.design.custom_view_tools.styles.StyleParamsProvider
import com.chekurda.design.custom_view_tools.utils.StaticLayoutConfigurator
import com.chekurda.design.custom_view_tools.utils.TextHighlights
import com.chekurda.design.custom_view_tools.utils.getTextWidth
import timber.log.Timber
import kotlin.math.roundToInt

/**
 * Разметка для отображения текста.
 *
 * @param config настройка параметров текстовой разметки.
 * @see TextLayoutParams
 *
 * Является оберткой над [Layout] для отображения текста,
 * который лениво создается по набору параметров модели [params].
 * Также содержит параметры и api ускоряющие и облегчающие работу с кастомной текстовой разметкой.
 *
 * Параметры разметки настраиваются с помощью конфига [TextLayoutConfig] в конструкторе,
 * или с помощью методов [configure] и [buildLayout].
 * Статичный метод [createTextLayoutByStyle] позволяет создавать разметку по xml стилю.
 *
 * Дополнительный функционал:
 * - Установка слушателя кликов [TextLayout.OnClickListener].
 * - Установка долгих кликов кликов [TextLayout.OnLongClickListener].
 * - Менеджер для упрошения обработки касаний по текстовым разметкам [TextLayoutTouchManager].
 * - Поддержка состояний [isEnabled], [isPressed], [isSelected], см. [colorStateList].
 * - Вспомогательный класс для предоставления инфоримации о разметках [TextLayout] для автотестирования кастомных [View].
 * - Отладка границ разметки при помощи локального включения [isInspectMode], см. [InspectHelper].
 */
class TextLayout(config: TextLayoutConfig? = null) : View.OnTouchListener {

    /**
     * Слушатель кликов по текстовой разметке.
     * @see TextLayout.setOnClickListener
     */
    fun interface OnClickListener {

        fun onClick(context: Context, layout: TextLayout)
    }

    /**
     * Слушатель долгих кликов по текстовой разметке.
     * @see TextLayout.setOnLongClickListener
     */
    fun interface OnLongClickListener {

        fun onLongClick(context: Context, layout: TextLayout)
    }

    companion object {

        /**
         * Создать текстовую разметку [TextLayout] по параметрам ресурса стиля [styleRes].
         *
         * @param styleProvider поставщик стилей [TextStyle].
         * @param obtainPadding true, если текстовая разметка должна получить отступы из стиля.
         * @param postConfig конфиг параметров текстовой разметки
         * для дополнительной настройки после инициализии из ресурса стиля.
         */
        fun createTextLayoutByStyle(
            context: Context,
            @StyleRes styleRes: Int,
            styleProvider: StyleParamsProvider<TextStyle>? = null,
            obtainPadding: Boolean = true,
            postConfig: TextLayoutConfig? = null
        ): TextLayout =
            createTextLayoutByStyle(context, StyleKey(styleRes), styleProvider, obtainPadding, postConfig)

        /**
         * Создать текстовую разметку [TextLayout] по ключу стиля [styleKey].
         * @see StyleKey
         *
         * Использовать для сценариев, когда значения атрибутов стиля [StyleKey.styleRes] могут зависеть от разных тем,
         * поэтому для правильного кэширования помимо ресурса стиля необходим дополнительный [StyleKey.tag].
         *
         * @param styleProvider поставщик стилей [TextStyle].
         * @param obtainPadding true, если текстовая разметка должна получить отступы из стиля.
         * @param postConfig конфиг параметров текстовой разметки
         * для дополнительной настройки после инициализии из ресурса стиля.
         */
        fun createTextLayoutByStyle(
            context: Context,
            styleKey: StyleKey,
            styleProvider: StyleParamsProvider<TextStyle>? = null,
            obtainPadding: Boolean = true,
            postConfig: TextLayoutConfig? = null
        ): TextLayout =
            if (styleKey.styleRes != 0) {
                val style = styleProvider?.getStyleParams(context, styleKey)
                    ?: CanvasStylesProvider.obtainTextStyle(context, styleKey)
                TextLayout {
                    paint = TextPaint(ANTI_ALIAS_FLAG).also {
                        it.textSize = style.textSize ?: it.textSize
                        it.color = style.textColor ?: it.color
                    }
                    text = style.text ?: text
                    layoutWidth = style.layoutWidth.takeIf { it != 0 } ?: layoutWidth
                    alignment = style.alignment ?: alignment
                    ellipsize = style.ellipsize ?: ellipsize
                    includeFontPad = style.includeFontPad ?: includeFontPad
                    maxLines = style.maxLines ?: maxLines
                    isVisible = style.isVisible ?: isVisible
                    if (obtainPadding) {
                        style.paddingStyle?.also { paddingStyle ->
                            padding = TextLayoutPadding(
                                paddingStyle.paddingStart,
                                paddingStyle.paddingTop,
                                paddingStyle.paddingEnd,
                                paddingStyle.paddingBottom
                            )
                        }
                    }
                    postConfig?.invoke(this)
                }
            } else TextLayout(postConfig)
    }

    /**
     * Параметры для создания текстовой разметки [layout].
     */
    private val params = TextLayoutParams()

    /**
     * Вспомогательный класс для обработки событий касаний по текстовой разметке.
     */
    private val touchHelper: TouchHelper by lazy { TouchHelper() }

    /**
     * Горизонтальный паддинг для обработки касаний (левый и правый).
     */
    private val horizontalTouchPadding: Pair<Int, Int>
        get() = touchHelper.horizontalTouchPadding

    /**
     * Вертикальный паддинг для обработки касаний (верхний и нижний).
     */
    private val verticalTouchPadding: Pair<Int, Int>
        get() = touchHelper.verticalTouchPadding

    /**
     * Вспомогательный класс для управления рисуемыми состояниями текстовой разметки.
     * @see colorStateList
     */
    private val drawableStateHelper: DrawableStateHelper by lazy { DrawableStateHelper() }

    /**
     * Вспомогательный класс для отладки текстовой разметки.
     * Для включения отладочного мода необходимо переключить [isInspectMode] в true.
     * Может оказаться крайне полезным на этапе интеграции [TextLayout].
     */
    private val inspectHelper = if (isInspectMode) InspectHelper() else null

    /**
     * Получить снимок состояния [TextLayout].
     */
    internal val state: TextLayoutState
        get() = TextLayoutState(
            params.copy(),
            cachedLayout,
            isLayoutChanged,
            textPos
        )

    init {
        config?.invoke(params)
    }

    /**
     * Получить текстовую разметку.
     * Имеет ленивую инициализацию.
     */
    private val layout: Layout
        get() = cachedLayout
            ?.takeIf { !isLayoutChanged }
            ?: updateStaticLayout()

    /**
     * Текущая текстовая разметка.
     * Лениво инициализируется при первом обращении к [layout], если разметка изменилась [isLayoutChanged].
     */
    private var cachedLayout: Layout? = null

    /**
     * Текущая ширина разметки без учета оступов.
     * Лениво инициализируется при первом обращении к [layout], если разметка изменилась [isLayoutChanged].
     */
    @Px
    private var cachedLayoutWidth: Int = 0
        get() = layout.let { field }

    /**
     * Признак необходимости в построении layout при следующем обращении
     * по причине изменившихся данных.
     */
    private var isLayoutChanged: Boolean = true

    /**
     * Позиция текста для рисования с учетом внутренних отступов (координата левого верхнего угла).
     */
    private var textPos = params.padding.start.toFloat() to params.padding.top.toFloat()

    /**
     * Координаты границ [TextLayout], полученные в [layout].
     */
    private var rect = Rect()

    /**
     * Идентификатор разметки.
     */
    @IdRes
    var id: Int = ResourcesCompat.ID_NULL

    /**
     * Текст разметки.
     */
    val text: CharSequence
        get() = params.text

    /**
     * Краска текста разметки.
     */
    val textPaint: TextPaint
        get() = params.paint

    /**
     * Видимость разметки.
     */
    val isVisible: Boolean
        get() = params.isVisible.let {
            if (!params.isVisibleWhenBlank) it && params.text.isNotBlank()
            else it
        }

    /**
     * Максимальное количество строк.
     */
    val maxLines: Int
        get() = params.maxLines

    /**
     * Количество строк текста в [TextLayout].
     *
     * Обращение к полю вызывает построение [StaticLayout], если ранее он еще не был создан,
     * или если [params] разметки были изменены путем вызова [configure],
     * в иных случаях лишнего построения не произойдет.
     */
    val lineCount: Int
        get() = layout.lineCount

    /**
     * Левая позиция разметки, установленная в [layout].
     */
    @get:Px
    val left: Int
        get() = rect.left

    /**
     * Верхняя позиция разметки, установленная в [layout].
     */
    @get:Px
    val top: Int
        get() = rect.top

    /**
     * Правая позиция разметки с учетом внутренних паддингов [left] + [width].
     */
    @get:Px
    val right: Int
        get() = rect.right

    /**
     * Нижняя позиция разметки с учетом внутренний паддингов [top] + [height].
     */
    @get:Px
    val bottom: Int
        get() = rect.bottom

    /**
     * Левый внутренний оступ разметки.
     */
    @get:Px
    val paddingStart: Int
        get() = params.padding.start

    /**
     * Верхний внутренний оступ разметки.
     */
    @get:Px
    val paddingTop: Int
        get() = params.padding.top

    /**
     * Првый внутренний оступ разметки.
     */
    @get:Px
    val paddingEnd: Int
        get() = params.padding.end

    /**
     * Нижний внутренний оступ разметки.
     */
    @get:Px
    val paddingBottom: Int
        get() = params.padding.bottom

    /**
     * Ширина всей разметки.
     *
     * Обращение к полю вызывает построение [StaticLayout], если ранее он еще не был создан,
     * или если [params] разметки были изменены путем вызова [configure],
     * в иных случаях лишнего построения не произойдет.
     */
    @get:Px
    val width: Int
        get() = if (isVisible) {
            params.layoutWidth
                ?: maxOf(
                    params.minWidth,
                    minOf(paddingStart + cachedLayoutWidth + paddingEnd, params.maxWidth ?: Integer.MAX_VALUE)
                )
        } else 0

    /**
     * Высота всей разметки.
     *
     * Обращение к полю вызывает построение [StaticLayout], если ранее он еще не был создан,
     * или если [params] разметки были изменены путем вызова [configure],
     * в иных случаях лишнего построения не произойдет.
     */
    @get:Px
    val height: Int
        get() = if (isVisible) {
            if (width != 0) {
                maxOf(
                    params.minHeight,
                    minOf(paddingTop + layout.height + paddingBottom, params.maxHeight ?: Integer.MAX_VALUE)
                )
            } else {
                params.minHeight
            }
        } else 0

    /**
     * Базовая линия текстовой разметки.
     *
     * Обращение к полю вызывает построение [StaticLayout], если ранее он еще не был создан,
     * или если [params] разметки были изменены путем вызова [configure],
     * в иных случаях лишнего построения не произойдет.
     */
    @get:Px
    val baseline: Int
        get() = paddingTop + layout.getLineBaseline(0)

    /**
     * Установить/получить список цветов текста для состояний.
     * @see isEnabled
     * @see isPressed
     * @see isSelected
     *
     * Для работы [ColorStateList] необходимо сделать разметку кликабельной [makeClickable],
     * а также доставлять события касаний с помощью [TextLayoutTouchManager] или самостоятельно в метод [onTouch].
     */
    var colorStateList: ColorStateList? = null
        set(value) {
            val isChanged = value != field
            field = value
            if (isChanged) drawableStateHelper.onColorStateListChanged()
        }

    /**
     * Установить/получить состояние доступности тестовой разметки.
     *
     * Если текстовая разметка недоступна - клики обрабатываться не будут.
     * @see colorStateList
     * @see makeClickable
     * @see setOnClickListener
     * @see setOnLongClickListener
     */
    var isEnabled: Boolean = true
        set(value) {
            val isChanged = field != value
            field = value
            if (isChanged) drawableStateHelper.setEnabled(value)
        }

    /**
     * Установить/получить нажатое состояние тестовой разметки.
     *
     * @see colorStateList
     */
    var isPressed: Boolean = false
        set(value) {
            val isChanged = field != value
            field = value
            if (isChanged) drawableStateHelper.setPressed(value)
        }

    /**
     * Установить/получить состояние выбранности текстовой разметки.
     *
     * @see colorStateList
     */
    var isSelected: Boolean = false
        set(value) {
            val isChanged = field != value
            field = value
            if (isChanged) drawableStateHelper.setSelected(value)
        }

    /**
     * Получить ожидаемую ширину разметки для текста [text] без создания [StaticLayout].
     */
    @Px
    fun getDesiredWidth(text: CharSequence): Int =
        paddingStart + params.paint.getTextWidth(text) + paddingEnd

    /**
     * Настроить разметку.
     * Если параметры изменятся - разметка будет построена при следующем обращении.
     *
     * Использовать для изменения закэшированных параметров [params],
     * созданных при инициализации или переданных ранее,
     * кэш статичной разметки при этом будет обновлен по новым параметрам при следующем обращении.
     *
     * @param config настройка параметров текстовой разметки.
     * @return true, если параметры изменились.
     */
    fun configure(
        config: TextLayoutConfig
    ): Boolean {
        val oldTextSize = params.paint.textSize
        val oldParams = params.copy()

        config.invoke(params)
        checkWarnings()

        val isTextSizeChanged = oldTextSize != params.paint.textSize
        return (oldParams != params || isTextSizeChanged).also { isChanged ->
            if (isChanged) isLayoutChanged = true
        }
    }

    /**
     * Построить разметку.
     *
     * Использовать для принудительного построения разметки на базе параметров [params],
     * при этом настройка [config] будет применена перед построением новой разметки.
     *
     * @param config настройка параметров текстовой разметки.
     * @return true, если разметка изменилась.
     */
    fun buildLayout(
        config: TextLayoutConfig? = null
    ): Boolean =
        config?.let { configure(it) }
            .also { if (isVisible) layout }
            ?: false

    /**
     * Обновить внутренние отступы.
     *
     * @return true, если отступы изменились.
     */
    fun updatePadding(
        start: Int = paddingStart,
        top: Int = paddingTop,
        end: Int = paddingEnd,
        bottom: Int = paddingBottom
    ): Boolean = with(params) {
        val oldPadding = padding
        padding = TextLayoutPadding(start, top, end, bottom)
        isLayoutChanged = oldPadding != padding || isLayoutChanged
        oldPadding != padding
    }

    /**
     * Разместить разметку на координате ([left],[top]).
     * Координата является позицией левого верхнего угла [TextLayout]
     *
     * Метод вызывает построение [StaticLayout], если ранее он еще не был создан,
     * или если [params] разметки были изменены путем вызова [configure],
     * в иных случаях лишнего построения не произойдет.
     */
    fun layout(@Px left: Int, @Px top: Int) {
        rect.set(
            left,
            top,
            left + width,
            top + height
        )
        textPos = left + paddingStart.toFloat() to top + paddingTop.toFloat()

        touchHelper.updateTouchRect()
        inspectHelper?.updatePositions()
    }

    /**
     * Нарисовать разметку.
     *
     * Рисуется именно кэш текстовой разметки [cachedLayout],
     * чтобы не допускать построения layout на [View.onDraw].
     */
    fun draw(canvas: Canvas) {
        cachedLayout?.let { layout ->
            if (!isVisible || params.text.isEmpty()) return
            inspectHelper?.draw(canvas)
            canvas.withTranslation(textPos.first, textPos.second) {
                layout.draw(this)
            }
        }
    }

    /**
     * Сделать текстовую разметку кликабельной.
     * @param parentView view, в которой находится текстовая разметка.
     *
     * Необходимо вызывать для включения обработки [onTouch].
     * @see TextLayoutTouchManager - менеджер, который автоматически включает кликабельность.
     */
    fun makeClickable(parentView: View) {
        touchHelper.init(parentView)
        drawableStateHelper.init(parentView)
    }

    /**
     * Установить слушателя кликов [listener] по текстовой разметке.
     * @see TextLayoutTouchManager
     *
     * Для включения обработки кликов разметка должна быть кликабельная [makeClickable].
     * В состоянии [isEnabled] == false - клики обрабатываться не будут.
     */
    fun setOnClickListener(listener: OnClickListener?) {
        touchHelper.setOnClickListener(listener)
    }

    /**
     * Установить слушателя долгих кликов [listener] по текстовой разметке.
     * @see TextLayoutTouchManager
     *
     * Для включения обработки долгих кликов разметка должна быть кликабельная [makeClickable].
     * В состоянии [isEnabled] == false - клики обрабатываться не будут.
     */
    fun setOnLongClickListener(listener: OnLongClickListener?) {
        touchHelper.setOnLongClickListener(listener)
    }

    /**
     * Установить отступы для увеличения области касания по текстовой разметке.
     *
     * Отступы будут применены к основным границам [TextLayout] после вызова [layout].
     * Фактически происходит расширение кликабельной области на заданные значения
     * и не влияет на размер и позиции разметки.
     */
    fun setTouchPadding(
        left: Int = horizontalTouchPadding.first,
        top: Int = verticalTouchPadding.first,
        right: Int = horizontalTouchPadding.second,
        bottom: Int = horizontalTouchPadding.second
    ) {
        touchHelper.setTouchPadding(left, top, right, bottom)
    }

    /**
     * Установить отступы [padding] по всему периметру для увеличения области касания по текстовой разметке.
     * @see setTouchPadding
     */
    fun setTouchPadding(padding: Int) {
        touchHelper.setTouchPadding(padding)
    }

    /**
     * Установить статичную область кликабельности текстовой разметки.
     *
     * При установке [rect] отступы из [setTouchPadding] перестанут работать.
     * Для сброса статичной области кликабельности необходимо передать [rect] == null.
     */
    fun setStaticTouchRect(rect: Rect?) {
        touchHelper.setStaticTouchRect(rect)
    }

    /**
     * Обработать событие касания.
     *
     * Для включения обработки событий касания необходимо сделать текстовую разметку кликабельной [makeClickable].
     */
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View, event: MotionEvent): Boolean =
        touchHelper.onTouch(event)
            ?.also { isHandled -> drawableStateHelper.checkPressedState(event.action, isHandled) }
            ?: false

    /**
     * Отменить событие касания.
     */
    fun onTouchCanceled() {
        drawableStateHelper.checkPressedState(MotionEvent.ACTION_CANCEL, true)
    }

    /**
     * Обновить разметку по набору параметров [params].
     * Если ширина в [params] не задана, то будет использована ширина текста.
     * Созданная разметка помещается в кэш [cachedLayout].
     */
    private fun updateStaticLayout(): Layout =
        StaticLayoutConfigurator.createStaticLayout(params.text, params.paint) {
            width = params.textWidth
            alignment = params.alignment
            ellipsize = params.ellipsize
            includeFontPad = params.includeFontPad
            maxLines = params.maxLines
            maxHeight = params.textMaxHeight
            highlights = params.highlights
            canContainUrl = params.canContainUrl
        }.also {
            isLayoutChanged = false
            cachedLayout = it
            updateCachedLayoutWidth()
        }

    /**
     * Обновить кэш ширины разметки без учета отступов.
     */
    private fun updateCachedLayoutWidth() {
        cachedLayoutWidth = if (layout.lineCount == 1 && params.needHighWidthAccuracy) {
            layout.getLineWidth(0).roundToInt()
        } else {
            layout.width
        }
    }

    private fun checkWarnings() {
        val layoutWidth = params.layoutWidth
        if (!BuildConfig.DEBUG || layoutWidth == null) return

        val minWidth = params.minWidth
        val maxWidth = params.maxWidth
        if (minWidth > 0 && layoutWidth < minWidth) {
            Timber.e(IllegalArgumentException("Потенциальная ошибка отображения TextLayout: значение параметра layoutWidth(${params.layoutWidth}) меньше minWidth(${params.minWidth}). Приоритетное значение размера - layoutWidth(${params.layoutWidth}). TextLayoutParams = $params"))
        }
        if (maxWidth != null && layoutWidth > maxWidth) {
            Timber.e(IllegalArgumentException("Потенциальная ошибка отображения TextLayout: значение параметра layoutWidth(${params.layoutWidth}) больше maxWidth(${params.maxWidth}). Приоритетное значение размера - layoutWidth(${params.layoutWidth}). TextLayoutParams = $params"))
        }
    }

    /**
     * Параметры для создания текстовой разметки [Layout] в [TextLayout].
     *
     * @property text текста разметки.
     * @property paint краска текста.
     * @property layoutWidth ширина разметки. Null -> WRAP_CONTENT.
     * @property alignment мод выравнивания текста.
     * @property ellipsize мод сокращения текста.
     * @property includeFontPad включить стандартные отступы шрифта.
     * @property maxLines максимальное количество строк.
     * @property isVisible состояние видимости разметки.
     * @property padding внутренние отступы разметки.
     * @property highlights модель для выделения текста.
     * @property minWidth минимальная ширина разметки.
     * @property minHeight минимальная высота разметки.
     * @property maxWidth максимальная ширина разметки.
     * @property maxHeight максимальная высота разметки с учетом [padding]. Необходима для автоматического подсчета [maxLines].
     * @property isVisibleWhenBlank мод скрытия разметки при пустом тексте, включая [padding].
     * @property canContainUrl true, если строка может содержать url. Влияет на точность сокращения текста
     * и скорость создания [StaticLayout]. (Использовать только для [maxLines] > 1, когда текст может содержать ссылки)
     * @property needHighWidthAccuracy true, если необходимо включить мод высокой точности ширины текста.
     * Механика релевантна для однострочных разметок с сокращением текста, к размерам которых привязаны другие элементы.
     * После сокращения текста [StaticLayout] не всегда имеет точные размеры строго по границам текста ->
     * иногда остается лишнее пространство, которое может оказаться критичным для отображения.
     * [needHighWidthAccuracy] решает эту проблему, но накладывает дополнительные расходы на вычисления при перестроении разметки.
     */
    data class TextLayoutParams(
        var text: CharSequence = StringUtils.EMPTY,
        var paint: TextPaint = TextPaint(ANTI_ALIAS_FLAG),
        @Px var layoutWidth: Int? = null,
        var alignment: Alignment = Alignment.ALIGN_NORMAL,
        var ellipsize: TruncateAt = TruncateAt.END,
        var includeFontPad: Boolean = true,
        var maxLines: Int = 1,
        var isVisible: Boolean = true,
        var padding: TextLayoutPadding = TextLayoutPadding(),
        var highlights: TextHighlights? = null,
        @Px var minWidth: Int = 0,
        @Px var minHeight: Int = 0,
        @Px var maxWidth: Int? = null,
        @Px var maxHeight: Int? = null,
        var isVisibleWhenBlank: Boolean = true,
        var canContainUrl: Boolean = false,
        var needHighWidthAccuracy: Boolean = false
    ) {

        /**
         * Ширина текста.
         */
        @get:Px
        internal val textWidth: Int
            get() {
                val layoutWidth = layoutWidth
                val horizontalPadding = padding.start + padding.end
                return if (layoutWidth != null) {
                    maxOf(layoutWidth - horizontalPadding, 0)
                } else {
                    val textWidth = paint.getTextWidth(text)
                    val minTextWidth = if (minWidth > 0) maxOf(minWidth - horizontalPadding, 0) else 0
                    val maxTextWidth = maxWidth?.let { maxOf(it - horizontalPadding, 0) } ?: Integer.MAX_VALUE

                    maxOf(minTextWidth, minOf(textWidth, maxTextWidth))
                }
            }

        /**
         * Максимальная высота текста.
         */
        @get:Px
        internal val textMaxHeight: Int?
            get() = maxHeight?.let { maxOf(it - padding.top - padding.bottom, 0) }
    }

    /**
     * Параметры отступов текстовой разметки [Layout] в [TextLayout].
     */
    data class TextLayoutPadding(
        @Px val start: Int = 0,
        @Px val top: Int = 0,
        @Px val end: Int = 0,
        @Px val bottom: Int = 0
    )

    /**
     * Вспомогательный класс для обработки касаний по [TextLayout].
     */
    private inner class TouchHelper {

        private var parentView: View? = null

        private val touchRect: Rect = Rect()
        private var isStaticTouchRect = false
        var horizontalTouchPadding = 0 to 0
            private set
        var verticalTouchPadding = 0 to 0
            private set

        private var gestureDetector: GestureDetector? = null
            get() {
                if (field == null) {
                    field = parentView?.context?.let { GestureDetector(it, gestureListener) }
                }
                return field
            }
        private val gestureListener = object : GestureDetector.SimpleOnGestureListener() {

            private fun isInTouchRect(event: MotionEvent) =
                touchRect.contains(event.x.roundToInt(), event.y.roundToInt())

            override fun onDown(event: MotionEvent): Boolean =
                isInTouchRect(event)

            override fun onSingleTapUp(event: MotionEvent): Boolean =
                (isInTouchRect(event)).also { isConfirmed ->
                    if (!isEnabled || !isConfirmed) return@also

                    val context = parentView?.context ?: return@also
                    onClickListener?.onClick(context, this@TextLayout)
                }

            override fun onLongPress(event: MotionEvent) {
                if (isInTouchRect(event) && isEnabled) {
                    val context = parentView?.context ?: return
                    onLongClickListener?.onLongClick(context, this@TextLayout)?.also {
                        parentView?.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                    }
                }
            }
        }
        private var onClickListener: OnClickListener? = null
        private var onLongClickListener: OnLongClickListener? = null

        /**
         * Проинициализировать помощника.
         *
         * @param parentView view, в которой находится текстовая разметка.
         */
        fun init(parentView: View) {
            this.parentView = parentView
        }

        /**
         * Установить слушателя кликов [listener] по текстовой разметке.
         * @see TextLayoutTouchManager
         *
         * Для включения обработки кликов разметка должна быть кликабельная [makeClickable].
         * В состоянии [isEnabled] == false - клики обрабатываться не будут.
         */
        fun setOnClickListener(listener: OnClickListener?) {
            onClickListener = listener
        }

        /**
         * Установить слушателя долгих кликов [listener] по текстовой разметке.
         * @see TextLayoutTouchManager
         *
         * Для включения обработки долгих кликов разметка должна быть кликабельная [makeClickable].
         * В состоянии [isEnabled] == false - клики обрабатываться не будут.
         */
        fun setOnLongClickListener(listener: OnLongClickListener?) {
            onLongClickListener = listener
        }

        /**
         * Установить отступы для увеличения области касания по текстовой разметке.
         *
         * Отступы будут применены к основным границам [TextLayout] после вызова [layout].
         * Фактически происходит расширение кликабельной области на заданные значения
         * и не влияет на размер и позиции разметки.
         */
        fun setTouchPadding(
            left: Int = horizontalTouchPadding.first,
            top: Int = verticalTouchPadding.first,
            right: Int = horizontalTouchPadding.second,
            bottom: Int = horizontalTouchPadding.second
        ) {
            horizontalTouchPadding = left to right
            verticalTouchPadding = top to bottom
        }

        /**
         * Установить отступы [padding] по всему периметру для увеличения области касания по текстовой разметке.
         * @see setTouchPadding
         */
        fun setTouchPadding(padding: Int) {
            setTouchPadding(left = padding, top = padding, right = padding, bottom = padding)
        }

        /**
         * Установить статичную область кликабельности текстовой разметки.
         *
         * При установке [rect] отступы из [setTouchPadding] перестанут работать.
         * Для сброса статичной области кликабельности необходимо передать [rect] == null.
         */
        fun setStaticTouchRect(rect: Rect?) {
            touchRect.set(rect ?: this@TextLayout.rect)
            isStaticTouchRect = touchRect != this@TextLayout.rect
        }

        /**
         * Обновить область касания согласно [TextLayout.rect].
         *
         * Игнорируется, если установлена статичная область касания [setStaticTouchRect].
         */
        fun updateTouchRect() {
            if (isStaticTouchRect) return
            with(rect) {
                touchRect.set(
                    left - horizontalTouchPadding.first,
                    top - verticalTouchPadding.first,
                    right + horizontalTouchPadding.second,
                    bottom + verticalTouchPadding.second
                )
            }
        }

        /**
         * Обработать событие касания [event].
         * @return true, если событие касания было обработано текущей текстовой разметкой.
         */
        fun onTouch(event: MotionEvent): Boolean? =
            gestureDetector?.onTouchEvent(event)
    }

    /**
     * Вспомогательный класс для управления рисуемыми состояниями текстовой разметки.
     * @see colorStateList
     */
    private inner class DrawableStateHelper {

        /**
         * Список текущих рисуемых состояний текстовой разметки.
         */
        private val drawableState = mutableSetOf(android.R.attr.state_enabled)
        private var parentView: View? = null

        /**
         * Проинициализировать помощника.
         *
         * @param parentView view, в которой находится текстовая разметка.
         */
        fun init(parentView: View) {
            this.parentView = parentView
        }

        /**
         * Колбэк об обновлении списка цветов для состояний - [colorStateList].
         */
        fun onColorStateListChanged() {
            updateTextColorByState()
        }

        /**
         * Проверить состояние нажатости по действию события касания [motionAction] и признаку обработки этого события [isHandled].
         */
        fun checkPressedState(motionAction: Int, isHandled: Boolean) {
            when (motionAction and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_DOWN,
                MotionEvent.ACTION_POINTER_DOWN -> {
                    if (isHandled) {
                        removeCancelPressedCallback()
                        if (isEnabled) setPressed(true)
                    }
                }
                MotionEvent.ACTION_UP           -> dispatchCancelPressedCallback()
                MotionEvent.ACTION_CANCEL       -> setPressed(false)
            }
        }

        /**
         * Установить доступное состояние тестовой разметки.
         */
        fun setEnabled(enabled: Boolean) {
            val enabledAttr = android.R.attr.state_enabled
            val disableAttr = -enabledAttr

            val isStateChanged = if (enabled) {
                val isAdded = drawableState.add(enabledAttr)
                val isRemoved = drawableState.remove(disableAttr)
                isAdded || isRemoved
            } else {
                val isAdded = drawableState.add(disableAttr)
                val isRemoved = drawableState.remove(enabledAttr)
                isPressed = false
                isAdded || isRemoved
            }

            if (isStateChanged) {
                updateTextColorByState()
                invalidate()
            }
        }

        /**
         * Установить нажатое состояние тестовой разметки.
         */
        fun setPressed(pressed: Boolean) {
            updateDrawableState(android.R.attr.state_pressed, pressed)
        }

        /**
         * Установить выбранное состояние текстовой разметки.
         */
        fun setSelected(selected: Boolean) {
            updateDrawableState(android.R.attr.state_selected, selected)
        }

        /**
         * Обновить рисуемое состояние текстовой разметки.
         *
         * @param stateAttr атрибут нового состояния
         * @param isActive true, если состояние активно
         */
        private fun updateDrawableState(@AttrRes stateAttr: Int, isActive: Boolean) {
            val isStateChanged =
                if (isActive) drawableState.add(stateAttr)
                else drawableState.remove(stateAttr)

            if (isStateChanged) {
                updateTextColorByState()
                invalidate()
            }
        }

        /**
         * Обновить цвет текста согласно текущему рисуемому состоянию.
         */
        private fun updateTextColorByState() {
            textPaint.drawableState = drawableState.toIntArray()
            colorStateList?.let { stateList ->
                textPaint.color = stateList.getColorForState(textPaint.drawableState, stateList.defaultColor)
            }
        }

        private fun invalidate() {
            parentView?.takeIf { colorStateList != null && it.isAttachedToWindow }
                ?.invalidate()
        }

        /**
         * Действие отмены нажатого рисуемого состояния.
         */
        private val cancelPressedCallback = Runnable { setPressed(false) }

        /**
         * Отправить отложенное действие [cancelPressedCallback] для отмены нажатого рисуемого состояния.
         */
        private fun dispatchCancelPressedCallback() {
            parentView?.handler?.postDelayed(
                cancelPressedCallback,
                ViewConfiguration.getPressedStateDuration().toLong()
            )
        }

        /**
         * Очистить колбэк для отмены нажатого рисуемого состояния [cancelPressedCallback].
         */
        private fun removeCancelPressedCallback() {
            parentView?.handler?.removeCallbacks(cancelPressedCallback)
        }
    }

    /**
     * Вспомогательный класс для отладки текстовой разметки.
     * Позволяет отображать границы [TextLayout], а также внутренние отступы.
     * Может оказаться крайне полезным на этапе интеграции [TextLayout].
     */
    private inner class InspectHelper {

        /**
         * Краска линии границы по периметру [TextLayout].
         */
        val borderPaint = Paint(ANTI_ALIAS_FLAG).apply {
            color = Color.RED
            style = Paint.Style.STROKE
        }

        /**
         * Краска внутренних отступов [TextLayout].
         */
        val paddingPaint = Paint(ANTI_ALIAS_FLAG).apply {
            color = Color.YELLOW
            style = Paint.Style.FILL
        }
        val borderPath = Path()
        val borderRectF = RectF()
        val paddingPath = Path()
        val textBackgroundPath = Path()

        /**
         * Обновить закэшированные позиции границ разметки.
         */
        fun updatePositions() {
            borderPath.reset()
            textBackgroundPath.reset()
            paddingPath.reset()

            borderRectF.set(
                left.toFloat() + ONE_PX,
                top.toFloat() + ONE_PX,
                right.toFloat() - ONE_PX,
                bottom.toFloat() - ONE_PX,
            )
            borderPath.addRect(borderRectF, Path.Direction.CW)

            textBackgroundPath.addRect(
                textPos.first,
                textPos.second,
                textPos.first + cachedLayoutWidth,
                textPos.second + layout.height,
                Path.Direction.CW
            )
            paddingPath.addRect(borderRectF, Path.Direction.CW)
            paddingPath.op(textBackgroundPath, Path.Op.DIFFERENCE)
        }

        /**
         * Нарисовать отладочные границы разметки.
         */
        fun draw(canvas: Canvas) {
            if (isVisible) {
                canvas.drawPath(paddingPath, paddingPaint)
                canvas.drawPath(borderPath, borderPaint)
            }
        }
    }

    /**
     * Модель внутреннего состояния [TextLayout].
     * @see TextLayout.params
     * @see TextLayout.cachedLayout
     * @see TextLayout.isLayoutChanged
     * @see TextLayout.textPos
     */
    internal data class TextLayoutState(
        val params: TextLayoutParams,
        val cachedLayout: Layout?,
        val isLayoutChanged: Boolean,
        val textPos: Pair<Float, Float>
    )
}

/**
 * Настройка для параметров [TextLayout.TextLayoutParams].
 */
typealias TextLayoutConfig = TextLayoutParams.() -> Unit

/**
 * Мод активации отладочных границ [TextLayout].
 * При включении дополнительно будут нарисованы границы вокруг [TextLayout], а также внутренние отступы.
 */
private const val isInspectMode = false
private const val ONE_PX = 1
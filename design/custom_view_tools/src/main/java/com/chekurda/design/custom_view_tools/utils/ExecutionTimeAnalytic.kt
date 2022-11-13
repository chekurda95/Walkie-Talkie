package com.chekurda.design.custom_view_tools.utils

import org.apache.commons.lang3.StringUtils.EMPTY
import timber.log.Timber

/**
 * Утилита для аналитики времени выполнения кода, которая позволяет узнать среднее время
 * создания класса или выполнения содержимого лямбды.
 * Эта информация полезна при произведении работ по оптимизации view и различных алгоритмов.
 *
 * Запускается посредством вызова [analyzeExecutionTime] и позволяет
 * оценить, сравнить среднее и общее время выполнения содержимого списка лямбд [Execution.invoke].
 *
 * Пример вызова:
 * ExecutionTimeAnalytic.analyzeExecutionTime(
 *    100,
 *    Execution("sum") { 2 + 2 },
 *    Execution { AppCompatTextView(requireContext()) },
 *    Execution { FrameLayout(requireContext()) },
 *    Execution { SimpleDraweeView(requireContext()) }
 * )
 * Мы передаем количество, например count = 100 - это будет количество исполнений для каждого действия,
 * чтобы оценить среднее время выполнения, а также набор действий.
 * Каждая из лямбд в качестве типа возвращает название класса результата (у 2 + 2 - это Integer),
 * поэтому при сравнении одинаковых типов можно добавить дополнительный тег при создании [Execution].
 *
 * После выполнения всех заданных действий в Logcat по типу verbose с фильтром "ExecutionTimeAnalytic"
 * можно увидеть следующую картину аналитики:
 *  <---- Analytic result for count 100 in ms ---->
 *  Top | Average |   Full  | Type
 *  1)  |  0,000  | 0,030   | Integer sum
 *  2)  |  0,069  | 6,874   | FrameLayout
 *  3)  |  0,124  | 12,388  | SimpleDraweeView
 *  4)  |  1,253  | 125,281 | AppCompatTextView
 *
 * В данном случае мы видим:
 * - Что самая быстрая операция суммирования - среднее время выполненения 0 мкс, общее при 100 вызовах - 30 мкс.
 * - На втором месте операция создания FrameLayout - среднее время создания 69 мкс, общее при 100 вызовах - 6.874 мс
 * - На четвертом месте операция создания AppCompatTextView - среднее время создания 1,253 мс, общее 125,281 мс
 *
 * На основании полученной из аналитики информации можно делать выводы и оптимизироваться там, где это необходимо.
 *
 * @author vv.chekurda
 */
object ExecutionTimeAnalytic {

    /**
     * Формат времени результата.
     */
    private const val RESULT_TIME_FORMAT = "%.3f"

    /**
     * Модель действия для аналитики.
     *
     * @property additionalTag дополнительный тег к названию класса полученного результата из [invoke].
     * @property invoke действие, которое будем анализировать.
     */
    data class Execution(val additionalTag: String = EMPTY, val invoke: () -> Any)

    /**
     * Получить текущее время в мс с точностью до наносекунд.
     */
    private val currentTime: Double
        get() = System.nanoTime().toDouble() / 1000000

    /**
     * Проанализировать время выполнения перечни действий [Execution] в количестве раз [count].
     *
     * Результат аналитики выводится в Logcat по типу verbose с фильтром "ExecutionTimeAnalytic".
     */
    fun analyzeExecutionTime(count: Int, vararg execution: Execution) {
        val result = StringBuilder().appendLine()
            .appendLine("<---- Analytic result for count $count in ms ---->")
            .appendLine("Top | Average |  Full  | Type")
        execution.map { getTypeWithFullTimeMs(it, count) }
            .sortedBy { it.second }
            .forEachIndexed { index, pair ->
                val averageTime = String.format(RESULT_TIME_FORMAT, pair.second / count)
                val fullTime = String.format(RESULT_TIME_FORMAT, pair.second)
                result.appendLine("${index + 1})  |  $averageTime  | $fullTime | ${pair.first}")
            }
        Timber.d(result.toString())
    }

    @Synchronized
    private fun getTypeWithFullTimeMs(execution: Execution, count: Int): Pair<String, Double> = with(execution) {
        val type = "${invoke()::class.java.simpleName } $additionalTag "
        val startTime = currentTime
        repeat(count) { execution.invoke() }
        val fullTimeMs = currentTime - startTime
        return type to fullTimeMs
    }
}
/**
 * Интструменты для работы со строками при тестировании.
 */
package ru.tensor.sbis.common.testing

import kotlin.random.Random

internal const val PREDICTED_TEXT_TEMPLATE = "Modification for string '%s'"

/**
 * Метод для модификации строки. Используется при тестировании реакций на изменение строки.
 * Во время проверок в тестах важно публиковать результат на случай обнаружения "особого сценария" в тестируемой функции
 *
 * ```
 * // изменение текста
 * val changedText = text.modify()
 * service.function(changedText)
 *
 * // публикация в консоль модифицированой строки
 * assertThat("Changed text is '$changedText'", ...)
 * ```
 *
 * @return непустая строка, которая содержит не более `length - 1` символов оригинальной строки
 */
fun CharSequence.modify(
    canBeBlank: Boolean = true,
    iteration: Int = 0,
    iterationLimit: Int = 10
): String {
    if (iteration > iterationLimit) {
        println("Iteration limit is reached. Applying predictable modification")
        // применение предсказуемой, но гарантированной модификации
        return String.format(PREDICTED_TEXT_TEMPLATE, this)
    }

    // должен быть по меньшей мере один символ иначе отработает проверка на пустую строку
    val size = Random.nextInt(1, length)
    val builder = StringBuilder(length)

    // модификация методом исключения символов из оригинальной строки
    for (c in this) {
        if (builder.length >= size) {
            break
        }
        if (Random.nextBoolean()) {
            builder.append(c)
        }
    }

    val result = builder.toString()
    // следующая итерация, если строка не изменилась
    return if (this == result || !canBeBlank && result.isBlank()) modify(canBeBlank, iteration + 1) else result
}
/**
 * Интструменты для тестирования на декартовом произведении параметров с JUnitParams.
 *
 * При тестировании с использованием `@CombinedParameters` (позволяет проводить тесты на декартовом произведении
 * параметров) есть недостаток - не поддерживается работа с собственными типами и комбинация из методов,
 * как в `@Parameters(method = "testParams1,testParams2")`
 */
package ru.tensor.sbis.common.testing

import androidx.annotation.IntDef

const val NULLABLE_RESULT_CODE = 0
const val EMPTY_RESULT_CODE = 1
const val NOT_EMPTY_RESULT_CODE = 2

internal const val EMPTY_TEXT = ""
internal const val NOT_EMPTY_TEXT = "Test text"

@Retention(AnnotationRetention.SOURCE)
@IntDef(NULLABLE_RESULT_CODE, EMPTY_RESULT_CODE, NOT_EMPTY_RESULT_CODE)
private annotation class Code

/**
 * Преобразование кодового значения параметра в строку
 *
 * Контракт:
 * - [NULLABLE_RESULT_CODE] -> `null`
 * - [EMPTY_RESULT_CODE] -> [emptyString]
 * - [NOT_EMPTY_RESULT_CODE] -> [string]
 */
fun stringParamMapper(@Code code: Int, emptyString: String = EMPTY_TEXT, string: String = NOT_EMPTY_TEXT) = when (code) {
    NULLABLE_RESULT_CODE  -> null
    EMPTY_RESULT_CODE     -> emptyString
    NOT_EMPTY_RESULT_CODE -> string
    else                  -> throw IllegalArgumentException("Unexpected code $code")
}

/**
 * Преобразование кодового значения параметра в список типа [T]
 *
 * Контракт:
 * - [NULLABLE_RESULT_CODE] -> `null`
 * - [EMPTY_RESULT_CODE] -> [emptyList]
 * - [NOT_EMPTY_RESULT_CODE] -> [listOf]
 */
fun <T> dataListParamMapper(@Code code: Int, data: T) = when (code) {
    NULLABLE_RESULT_CODE  -> null
    EMPTY_RESULT_CODE     -> emptyList()
    NOT_EMPTY_RESULT_CODE -> listOf(data)
    else                  -> throw IllegalArgumentException("Unexpected code $code")
}
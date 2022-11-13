/**
 * Dsl для параметризованных тестов и класс хэлпер для него.
 */

package ru.tensor.sbis.common.testing

private typealias ArrayOfAny = Array<out Any?>
private typealias ArrayOfArraysOfAny = Array<ArrayOfAny>

@DslMarker
private annotation class ParamsDslDSLMarker

/**
 * Dsl для упрощённого создания параметров для JUnitParams https://www.baeldung.com/junit-params
 * Тестовый метод помечается аннотацией `@Parameters(method = "intParams")`, где `intParams` - имя метода,
 * который возвращает параметры.
 *
 * Пример:
 *
 * ```
 * fun intParams() =
 *      params {
 *          add(1, 2)
 *          add(3, 4)
 *      }
 * ```
 *
 * Два метода для добавления параметров ParamsDslHelper [ParamsDslHelper.add] и [ParamsDslHelper.addAll]
 *
 * @return массив массивов с параметрами для тестов
 */
fun params(init: ParamsDslHelper.() -> Unit): ArrayOfArraysOfAny = ParamsDslHelper().apply(init).build()

/**
 * Класс-хэлпер для dsl параметров [params]
 */
class ParamsDslHelper {

    private val list = mutableListOf<ArrayOfAny>()

    private val isNotEmpty: Boolean
        get() = list.isNotEmpty() && list[0].isNotEmpty()

    private val allArraysAreEqualsBySize: Boolean
        get() = list.all { it.size == list.first().size }

    /**
     * Добавление набора параметров.
     * Количество в рамках одного тестовового метода должна быть одинаковым,
     * то есть каждый вызов [add] с одинаковым количество параметров.
     *
     * @param value параметр или несколько параметров (набор параметров)
     */
    @ParamsDslDSLMarker
    fun add(vararg value: Any?) {
        list.add(value)
    }

    /**
     * Добавление списка параметров. Каждый элемент списка станет набором параметров,
     * а каждый набор будет содержать по одному параметру.
     *
     * Например, `addAll(listOf(1,2,3))` будет аналогом `arrayOf(arrayOf(1), arrayOf(2), arrayOf(3))`
     *
     * @param params список параметров для добавления
     */
    @ParamsDslDSLMarker
    fun addAll(params: Collection<Any?>) {
        params.mapTo(list) { arrayOf(it) }
    }

    internal fun build(): ArrayOfArraysOfAny {
        check(isNotEmpty) {
            "Parameters must not be empty"
        }

        check(allArraysAreEqualsBySize) {
            "All parameters arrays must be the same sizes"
        }

        return list.toTypedArray()
    }
}

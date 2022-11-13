package ru.tensor.sbis.common.testing

import org.junit.Assert.assertArrayEquals
import org.junit.Test

class TestParamsDslTest {

    @Test(expected = IllegalStateException::class)
    fun `Given empty params, when build params, then throw IllegalStateException`() {
        params { }
    }

    @Test(expected = IllegalStateException::class)
    fun `Given params with different sized arrays, when build params, then throw IllegalStateException`() {
        params {
            add(1)
            add(1, 2)
        }
    }

    @Test
    fun `When adding single params, then return array of arrays with single element`() {
        val testArray: Array<Array<out Any?>> =
            arrayOf(
                arrayOf(1),
                arrayOf(2),
                arrayOf(3)
            )
        val paramsArray =
            params {
                add(1)
                add(2)
                add(3)
            }

        assertArrayEquals(testArray, paramsArray)
    }

    @Test
    fun `When adding single params with addAll Collection, then return array of arrays with single element`() {
        val testArray: Array<Array<out Any?>> =
            arrayOf(
                arrayOf(1),
                arrayOf(2),
                arrayOf(3)
            )
        val paramsArray =
            params {
                addAll(listOf(1, 2, 3))
            }

        assertArrayEquals(testArray, paramsArray)
    }

    @Test
    fun `When adding double params, then return array of arrays with two elements`() {
        val testArray: Array<Array<out Any?>> =
            arrayOf(
                arrayOf(1, "g"),
                arrayOf(2, "gh"),
                arrayOf(3, "ghj")
            )
        val paramsArray =
            params {
                add(1, "g")
                add(2, "gh")
                add(3, "ghj")
            }

        assertArrayEquals(testArray, paramsArray)
    }

    @Test
    fun `When adding multiple params, then return array of arrays with given number of elements`() {
        val testArray: Array<Array<out Any?>> =
            arrayOf(
                arrayOf(1, "g", 77L, 22f),
                arrayOf(2, "gh", 79L, 25f),
                arrayOf(3, "ghj", 776L, 22.3f),
                arrayOf(555, "ghjk", 7767L, 227.3f)
            )
        val paramsArray =
            params {
                add(1, "g", 77L, 22f)
                add(2, "gh", 79L, 25f)
                add(3, "ghj", 776L, 22.3f)
                add(555, "ghjk", 7767L, 227.3f)
            }

        assertArrayEquals(testArray, paramsArray)
    }
}

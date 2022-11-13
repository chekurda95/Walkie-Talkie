package ru.tensor.sbis.common.testing

import com.nhaarman.mockitokotlin2.mock
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

private const val ILLEGAL_CODE = 3

@RunWith(JUnitParamsRunner::class)
class ParamsKtTest {

    @Test
    @Parameters(value = ["$EMPTY_RESULT_CODE, $EMPTY_TEXT", "$NOT_EMPTY_RESULT_CODE, $NOT_EMPTY_TEXT"])
    fun `Given string's code id, when code id isn't equals to NULLABLE_RESULT_CODE, then string will be equal to expected text`(
        code: Int,
        resultText: String
    ) {
        val string = stringParamMapper(code)

        assertTrue(string == resultText)
    }

    @Test
    fun `Given string's code id, when code id equals to NULLABLE_RESULT_CODE, then string will be null`() {
        val string = stringParamMapper(NULLABLE_RESULT_CODE)

        assertNull(string)
    }

    @Test
    fun `Given string's code id and custom empty string, when code id equals to EMPTY_RESULT_CODE, then string will be equal to custom empty string`() {
        val customEmptyText = EMPTY_TEXT + mock()
        val string = stringParamMapper(EMPTY_RESULT_CODE, emptyString = customEmptyText)

        assertTrue(string == customEmptyText)
    }

    @Test
    fun `Given string's code id and custom not empty string, when code id equals to EMPTY_RESULT_CODE, then string will be equal to custom not empty string`() {
        val customNotEmptyText = NOT_EMPTY_TEXT + mock()
        val string = stringParamMapper(NOT_EMPTY_RESULT_CODE, string = customNotEmptyText)

        assertTrue(string == customNotEmptyText)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `Given string's code id, when code id is illegal code, then will be thrown an Exception`() {
        stringParamMapper(ILLEGAL_CODE)
    }

    @Test
    fun `Given code id and data type, when code id equals to NULLABLE_RESULT_CODE, then list will be null`() {
        val list = dataListParamMapper(NULLABLE_RESULT_CODE, mock<Any>())

        assertNull(list)
    }

    @Test
    fun `Given code id and data type, when code id equals to EMPTY_RESULT_CODE, then list will be empty`() {
        val list = dataListParamMapper(EMPTY_RESULT_CODE, mock<Any>())

        assertTrue(list!!.isEmpty())
    }

    @Test
    fun `Given code id and data type, when code id equals to NOT_EMPTY_RESULT_CODE, then list won't be empty`() {
        val data = mock<Any>()
        val list = dataListParamMapper(NOT_EMPTY_RESULT_CODE, data)

        assertEquals(listOf(data), list)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `Given code id, when code id is illegal code, then will be thrown an Exception`() {
        dataListParamMapper(ILLEGAL_CODE, mock<Any>())
    }
}
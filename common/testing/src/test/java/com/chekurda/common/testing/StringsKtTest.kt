package ru.tensor.sbis.common.testing

import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

private const val TEXT = "Test text"
private const val START_ITERATION = 0
private const val MAX_ITERATION_LIMIT = 15

@RunWith(JUnitParamsRunner::class)
class StringsKtTest {

    @Test
    @Parameters(value = ["$START_ITERATION, false", "$MAX_ITERATION_LIMIT, true"])
    fun `Given string and start iteration, when start iteration is less or equal to max iteration limit, then string won't be equal to a predicted string`(
        iteration: Int,
        valid: Boolean
    ) {
        val string = TEXT.modify(iteration = iteration)

        assertEquals(string == getPredictedText(), valid)
    }

    @Test
    @Parameters(value = ["$START_ITERATION, false", "$MAX_ITERATION_LIMIT, false", "${MAX_ITERATION_LIMIT + 1}, true"])
    fun `Given string, start iteration and custom max iteration limit, when start iteration is less or equal to custom max iteration limit, then string won't be equal to a predicted string`(
        iteration: Int,
        valid: Boolean
    ) {
        val string = TEXT.modify(iteration = iteration, iterationLimit = MAX_ITERATION_LIMIT)

        assertEquals(string == getPredictedText(), valid)
    }

    private fun getPredictedText() = String.format(PREDICTED_TEXT_TEMPLATE, TEXT)
}
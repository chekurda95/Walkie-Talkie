package com.chekurda.design.custom_view_tools.utils.view_extensions

import android.content.res.Resources
import android.util.DisplayMetrics
import android.util.DisplayMetrics.DENSITY_DEFAULT
import android.view.View
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import ru.tensor.sbis.common.testing.params
import com.chekurda.design.custom_view_tools.utils.dp
import com.chekurda.design.custom_view_tools.utils.mathRoundToInt
import com.chekurda.design.custom_view_tools.utils.sp

/**
 * Тексты методов [dp], [sp].
 *
 * @author vv.chekurda
 */
@RunWith(JUnitParamsRunner::class)
class CustomViewExtensionsKtDimensCalculationTest {

    private val mockView: View = mock()
    private val mockResources: Resources = mock()
    private lateinit var displayMetrics: DisplayMetrics

    @Before
    fun setUp() {
        displayMetrics = DisplayMetrics().apply {
            density = DENSITY_DEFAULT.toFloat()
            scaledDensity = DENSITY_DEFAULT * 1.5f
        }
        whenever(mockView.resources).thenReturn(mockResources)
        whenever(mockResources.displayMetrics).thenReturn(displayMetrics)
    }

    @Test
    @Parameters(method = "paramsWithFloatValues")
    fun `dp(Float) algorithm test`(value: Float) {
        val density = displayMetrics.density
        val expectedPx = (density * value).mathRoundToInt()

        val result = mockView.dp(value)

        assertEquals(expectedPx, result)
        assertNotEquals(0f, density)
        assertNotEquals(density, displayMetrics.scaledDensity)
    }

    @Test
    @Parameters(method = "paramsWithIntValues")
    fun `dp(Int) algorithm test`(value: Int) {
        val density = displayMetrics.density
        val expectedPx = (density * value).mathRoundToInt()

        val result = mockView.dp(value)

        assertEquals(expectedPx, result)
        assertNotEquals(0f, density)
        assertNotEquals(density, displayMetrics.scaledDensity)
    }

    @Test
    @Parameters(method = "paramsForRoundingTest")
    fun `dp(Float) mathematical rounding test`(value: Float, expected: Int) {
        displayMetrics.density = 1f

        assertEquals(expected, mockView.dp(value))
    }

    @Test
    @Parameters(method = "paramsWithFloatValues")
    fun `sp(Float) algorithm test`(value: Float) {
        val scaledDensity = displayMetrics.scaledDensity
        val expectedPx = (scaledDensity * value).mathRoundToInt()

        val result = mockView.sp(value)

        assertEquals(expectedPx, result)
        assertNotEquals(0f, scaledDensity)
        assertNotEquals(scaledDensity, displayMetrics.density)
    }

    @Test
    @Parameters(method = "paramsWithIntValues")
    fun `sp(Int) algorithm test`(value: Int) {
        val scaledDensity = displayMetrics.scaledDensity
        val expectedPx = (scaledDensity * value).mathRoundToInt()

        val result = mockView.sp(value)

        assertEquals(expectedPx, result)
        assertNotEquals(0f, scaledDensity)
        assertNotEquals(scaledDensity, displayMetrics.density)
    }


    @Test
    @Parameters(method = "paramsForRoundingTest")
    fun `sp(Float) mathematical rounding test`(value: Float, expected: Int) {
        displayMetrics.scaledDensity = 1f

        assertEquals(expected, mockView.sp(value))
    }

    @Suppress("unused")
    private fun paramsWithFloatValues() = params {
        addAll(listOf(0f, 3f, 5.42312f, -1f, -2.5f))
    }

    @Suppress("unused")
    private fun paramsWithIntValues() = params {
        addAll(listOf(0, 1, 5, -1))
    }

    @Suppress("unused")
    private fun paramsForRoundingTest() = params {
        add(0f, 0)
        add(3f, 3)
        add(3.5f, 4)
        add(4.49f, 4)
        add(-2.5f, -3)
        add(-3.49f, -3)
    }
}
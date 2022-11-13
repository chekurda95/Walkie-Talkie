package com.chekurda.design.custom_view_tools.utils

import android.os.Build
import android.view.View.MeasureSpec
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import com.chekurda.design.custom_view_tools.utils.MeasureSpecUtils.makeAtMostSpec
import com.chekurda.design.custom_view_tools.utils.MeasureSpecUtils.makeExactlySpec
import com.chekurda.design.custom_view_tools.utils.MeasureSpecUtils.makeUnspecifiedSpec
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Тесты методов [makeExactlySpec], [makeAtMostSpec], [makeUnspecifiedSpec].
 *
 * @author vv.chekurda
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.P])
class MeasureSpecUtilsTest {

    @Test
    fun `When call makeExactlySpec(), then return spec with EXACTLY mode`() {
        val size = 50

        val exactlySpec = makeExactlySpec(size)
        val specMode = MeasureSpec.getMode(exactlySpec)
        val specSize = MeasureSpec.getSize(exactlySpec)

        assertEquals(MeasureSpec.EXACTLY, specMode)
        assertEquals(size, specSize)
    }

    @Test
    fun `When call makeAtMost(), then return spec with AT_MOST mode`() {
        val size = 50

        val atMostSpec = makeAtMostSpec(size)
        val specMode = MeasureSpec.getMode(atMostSpec)
        val specSize = MeasureSpec.getSize(atMostSpec)

        assertEquals(MeasureSpec.AT_MOST, specMode)
        assertEquals(size, specSize)
    }

    @Test
    fun `When call makeUnspecifiedSpec(), then return spec with UNSPECIFIED mode`() {
        val unspecifiedSpec = makeUnspecifiedSpec()
        val specMode = MeasureSpec.getMode(unspecifiedSpec)
        val specSize = MeasureSpec.getSize(unspecifiedSpec)

        assertEquals(MeasureSpec.UNSPECIFIED, specMode)
        assertEquals(0, specSize)
    }
}
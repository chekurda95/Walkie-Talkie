package com.chekurda.design.custom_view_tools.utils.view_extensions

import android.view.View
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import com.chekurda.design.custom_view_tools.utils.safeRequestLayout

/**
 * Тесты метода [safeRequestLayout].
 *
 * @author vv.chekurda
 */
@RunWith(MockitoJUnitRunner.StrictStubs::class)
class CustomViewExtensionsKtSafeRequestLayoutTest {

    @Mock
    private lateinit var mockView: View

    @Test
    fun `When layout is not requested, then call requestLayout()`() {
        whenever(mockView.isLayoutRequested).thenReturn(false)

        mockView.safeRequestLayout()

        verify(mockView).requestLayout()
        verify(mockView).isLayoutRequested
        verify(mockView).invalidate()
        verifyNoMoreInteractions(mockView)
    }

    @Test
    fun `When layout was requested, then don't call requestLayout()`() {
        whenever(mockView.isLayoutRequested).thenReturn(true)

        mockView.safeRequestLayout()

        verify(mockView, never()).requestLayout()
        verify(mockView).isLayoutRequested
        verify(mockView).invalidate()
        verifyNoMoreInteractions(mockView)
    }
}
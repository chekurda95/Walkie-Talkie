package ru.tensor.sbis.common.testing

import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableDouble
import androidx.databinding.ObservableField
import androidx.databinding.ObservableInt

fun <T> assertObservableValueEquals(
        expected: T,
        actual: ObservableField<in T>
) {
    org.junit.Assert.assertEquals(
            expected,
            actual.get()
    )
}

fun assertObservableValueEquals(
        expected: Int,
        actual: ObservableInt
) {
    org.junit.Assert.assertEquals(
            expected,
            actual.get()
    )
}

fun assertObservableValueEquals(
        expected: Double,
        actual: ObservableDouble
) {
    org.junit.Assert.assertEquals(
            expected,
            actual.get(),
            0.0
    )
}

fun <T> assertObservableValueNotEquals(
        expected: T,
        actual: ObservableField<T>
) {
    org.junit.Assert.assertNotEquals(
            expected,
            actual.get()
    )
}

fun assertObservableValueFalse(observable: ObservableBoolean) {
    org.junit.Assert.assertFalse(observable.get())
}

fun assertObservableValueTrue(observable: ObservableBoolean) {
    org.junit.Assert.assertTrue(observable.get())
}
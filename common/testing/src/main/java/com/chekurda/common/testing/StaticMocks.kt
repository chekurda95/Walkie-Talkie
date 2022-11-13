/**
 * Набор расширений для статических моков в стиле mockito-kotlin
 *
 * Примеры использования:
 *
 *  ```
 *  val mock = mockStatic<SomeClass>()
 *  mock.on<SomeClass, Int> { SomeClass.staticMethod() } doReturn 111
 *  assertEquals(111, SomeClass.staticMethod())
 *  ```
 *
 *  ```
 *  mockStatic<SomeClass>(){
 *      on<Int> { SomeClass.staticMethod() } doReturn 111
 *  }
 *  assertEquals(111, SomeClass.staticMethod())
 *  ```
 *
 *  Если в разных методах тестового класса используются разные моки одного и того же статического метода,
 *  необходимо каждый раз закрывать мок, иначе будет эксепшен:
 *
 *  ```
 *  val myMock = mockStatic<SomeClass>()
 *  // ....
 *  myMock.close()
 *  ```
 */

package ru.tensor.sbis.common.testing

import org.mockito.MockedStatic
import org.mockito.Mockito
import org.mockito.stubbing.OngoingStubbing

inline fun <reified T> mockStatic(): MockedStatic<T> = Mockito.mockStatic(T::class.java)

inline fun <reified T> mockStatic(init: MockStaticHelper<T>.() -> Unit): MockedStatic<T> =
    Mockito.mockStatic(T::class.java).apply {
        MockStaticHelper(this).apply(init)
    }

inline fun <T, R> MockedStatic<T>.on(crossinline call: () -> Unit): OngoingStubbing<R> = `when`<R> { call() }

infix fun <T> OngoingStubbing<T>.doReturn(value: T): OngoingStubbing<T> = thenReturn(value)

class MockStaticHelper<T>(val mock: MockedStatic<T>) {
    inline fun <R> on(crossinline call: () -> Unit): OngoingStubbing<R> = mock.`when`<R> { call() }
}

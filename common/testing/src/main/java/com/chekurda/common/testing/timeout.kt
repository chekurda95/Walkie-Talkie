/**
 * Рекомандованные ограничения на время исполнения асинхронных тестов.
 * При использовании из Java могут быть предупреждения "Attribute value must be constant", которые не оказывают
 * влияние на исполнение тестов.
 */
@file:JvmName("TestTimeout")
package ru.tensor.sbis.common.testing

const val FAST_TEST_TIMEOUT = 500L
const val MEDIUM_TEST_TIMEOUT = 1000L
const val SLOW_TEST_TIMEOUT = 2000L
const val DEFAULT_TEST_TIMEOUT = FAST_TEST_TIMEOUT

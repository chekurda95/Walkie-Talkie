package com.chekurda.common.plugin_struct

/**
 * Маркерный интерфейс для отметки публичного функционала модуля(публичного API)
 */
interface Feature

/**
 * Поставщик публичного API [Feature]. Позволяет добиться ленивости при инициализации плагинов.
 */
fun interface FeatureProvider<F : Feature> {
    fun get(): F
}

/**
 * Маркерный интерфейс для обозначения объекта, выполняющего функции регистрации и управления плагинами.
 */
interface FeatureRegistry

/**
 * Контейнер, содержащий ссылки на класс типа [Feature] и поставщик [FeatureProvider].
 */
data class FeatureWrapper<F : Feature>(
    val type: Class<F>,
    val provider: FeatureProvider<out F>
)
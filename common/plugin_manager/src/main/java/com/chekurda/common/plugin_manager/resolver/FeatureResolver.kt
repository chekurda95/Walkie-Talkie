package com.chekurda.common.plugin_manager.resolver

import com.chekurda.common.plugin_manager.PluginManager
import com.chekurda.common.plugin_struct.Feature
import com.chekurda.common.plugin_struct.FeatureProvider
import com.chekurda.common.plugin_struct.Plugin
import kotlin.jvm.Throws

/**
 * Разрешение конфликтных ситуаций при подключении модулей.
 * Позволяет указать однозначное соответствие между интерфейсом и реализацией API, которое необходимо плагину потребителю.
 */
interface FeatureResolver {

    @Throws(SingleDependencyNotResolvedException::class)
    fun resolveRequiredSingle(
        featureType: Class<out Feature>,
        caller: Plugin<*>,
        records: Set<PluginManager.Record<out Feature>>
    ): FeatureProvider<out Feature>

    @Throws(SingleDependencyNotResolvedException::class)
    fun resolveOptionalSingle(
        featureType: Class<out Feature>,
        caller: Plugin<*>,
        records: Set<PluginManager.Record<out Feature>>
    ): FeatureProvider<out Feature>?

    fun resolveRequiredMulti(
        featureType: Class<out Feature>,
        caller: Plugin<*>,
        records: Set<PluginManager.Record<out Feature>>
    ): Set<FeatureProvider<out Feature>>

    fun resolveOptionalMulti(
        featureType: Class<out Feature>,
        caller: Plugin<*>,
        records: Set<PluginManager.Record<out Feature>>
    ): Set<FeatureProvider<out Feature>>?


    class SingleDependencyNotResolvedException(
        featureType: Class<out Feature>,
        plugin: Plugin<*>,
        records: Set<PluginManager.Record<out Feature>>
    ) : RuntimeException("Не удалось определить зависимость ${featureType.canonicalName} для плагина ${plugin::class.java.canonicalName} среди множества поставщиков [${records.joinToString { it.supplier::class.java.canonicalName!! }}]")
}
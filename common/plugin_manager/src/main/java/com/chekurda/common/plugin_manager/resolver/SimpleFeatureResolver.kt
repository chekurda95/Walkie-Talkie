package com.chekurda.common.plugin_manager.resolver

import com.chekurda.common.plugin_manager.PluginManager
import com.chekurda.common.plugin_struct.Feature
import com.chekurda.common.plugin_struct.FeatureProvider
import com.chekurda.common.plugin_struct.Plugin

/**
 * Реализация [FeatureResolver].
 */
open class SimpleFeatureResolver : FeatureResolver {

    override fun resolveRequiredSingle(
        featureType: Class<out Feature>,
        caller: Plugin<*>,
        records: Set<PluginManager.Record<out Feature>>
    ): FeatureProvider<out Feature> {
        return when(records.size) {
            0 -> throw IllegalArgumentException()
            1 -> records.first().feature
            else -> throw FeatureResolver.SingleDependencyNotResolvedException(
                featureType,
                caller,
                records
            )
        }
    }

    override fun resolveOptionalSingle(
        featureType: Class<out Feature>,
        caller: Plugin<*>,
        records: Set<PluginManager.Record<out Feature>>
    ): FeatureProvider<out Feature>? {
        return when(records.size) {
            0 -> throw IllegalArgumentException()
            1 -> records.first().feature
            else -> throw FeatureResolver.SingleDependencyNotResolvedException(
                featureType,
                caller,
                records
            )
        }
    }

    override fun resolveRequiredMulti(
        featureType: Class<out Feature>,
        caller: Plugin<*>,
        records: Set<PluginManager.Record<out Feature>>
    ): Set<FeatureProvider<out Feature>> {
        return records.map { it.feature }.toSet()
    }

    override fun resolveOptionalMulti(
        featureType: Class<out Feature>,
        caller: Plugin<*>,
        records: Set<PluginManager.Record<out Feature>>
    ): Set<FeatureProvider<out Feature>>? {
        return records.map { it.feature }.toSet()
    }
}
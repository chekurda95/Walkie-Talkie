package com.chekurda.common.plugin_manager.resolver

import com.chekurda.common.plugin_manager.PluginManager
import com.chekurda.common.plugin_struct.Feature
import com.chekurda.common.plugin_struct.FeatureProvider
import com.chekurda.common.plugin_struct.Plugin

typealias ResolveAction<OUTPUT> = (
    featureType: Class<out Feature>,
    caller: Plugin<*>,
    records: Set<PluginManager.Record<out Feature>>,
    fallback: (featureType: Class<out Feature>, caller: Plugin<*>, records: Set<PluginManager.Record<out Feature>>) -> OUTPUT
) -> OUTPUT

/**
 * Реализация [FeatureResolver], позволяющая гибче подстраивать поведение.
 */
class FlexibleFeatureResolver(
    private val fallbackResolver: FeatureResolver = SimpleFeatureResolver(),
    private val onResolveRequiredSingle: ResolveAction<FeatureProvider<out Feature>> = { featureType, caller, records, fallback-> fallback(featureType, caller, records) },
    private val onResolveOptionalSingle: ResolveAction<FeatureProvider<out Feature>?> = { featureType, caller, records, fallback-> fallback(featureType, caller, records) },
    private val onResolveRequiredMulti: ResolveAction<Set<FeatureProvider<out Feature>>> = { featureType, caller, records, fallback-> fallback(featureType, caller, records) },
    private val onResolveOptionalMulti: ResolveAction<Set<FeatureProvider<out Feature>>?> = { featureType, caller, records, fallback-> fallback(featureType, caller, records) }
) : FeatureResolver {

    override fun resolveRequiredSingle(
        featureType: Class<out Feature>,
        caller: Plugin<*>,
        records: Set<PluginManager.Record<out Feature>>
    ): FeatureProvider<out Feature> =
        onResolveRequiredSingle(featureType, caller, records, fallbackResolver::resolveRequiredSingle)

    override fun resolveOptionalSingle(
        featureType: Class<out Feature>,
        caller: Plugin<*>,
        records: Set<PluginManager.Record<out Feature>>
    ): FeatureProvider<out Feature>? =
        onResolveOptionalSingle(featureType, caller, records, fallbackResolver::resolveOptionalSingle)

    override fun resolveRequiredMulti(
        featureType: Class<out Feature>,
        caller: Plugin<*>,
        records: Set<PluginManager.Record<out Feature>>
    ): Set<FeatureProvider<out Feature>> =
        onResolveRequiredMulti(featureType, caller, records, fallbackResolver::resolveRequiredMulti)

    override fun resolveOptionalMulti(
        featureType: Class<out Feature>,
        caller: Plugin<*>,
        records: Set<PluginManager.Record<out Feature>>
    ): Set<FeatureProvider<out Feature>>? =
        onResolveOptionalMulti(featureType, caller, records, fallbackResolver::resolveOptionalMulti)
}
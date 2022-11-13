package com.chekurda.common.plugin_manager.fake

import com.chekurda.common.plugin_struct.*

class Plugin4 : BasePlugin<Unit>() {

    internal lateinit var feature1Provider: Set<FeatureProvider<TestFeature1>>

    override val api: Set<FeatureWrapper<out Feature>> = emptySet()

    override val dependency: Dependency = Dependency.Builder()
        .requireSet(TestFeature1::class.java) { feature1Provider = it }
        .build()

    override val customizationOptions: Unit = Unit

    override fun initialize() {
        feature1Provider.first().get()
    }

}
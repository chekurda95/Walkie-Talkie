package com.chekurda.common.plugin_manager.fake

import com.chekurda.common.plugin_struct.*

class Plugin1 : BasePlugin<Unit>() {
    internal val testFeature1 = object : TestFeature1 {}

    internal var feature2Provider: FeatureProvider<TestFeature2>? = null

    override val api: Set<FeatureWrapper<out Feature>> = setOf(
        FeatureWrapper(TestFeature1::class.java) {
            testFeature1
        }
    )

    override val dependency: Dependency = Dependency.Builder()
        .optional(TestFeature2::class.java) { feature2Provider = it }
        .build()

    override val customizationOptions: Unit = Unit

    override fun initialize() {
        feature2Provider?.get()
    }

}
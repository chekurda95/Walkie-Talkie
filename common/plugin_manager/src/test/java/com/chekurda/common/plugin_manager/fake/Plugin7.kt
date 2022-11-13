package com.chekurda.common.plugin_manager.fake

import com.chekurda.common.plugin_struct.*

class Plugin7 : BasePlugin<Unit>() {

    internal lateinit var testFeature1: FeatureProvider<TestFeature1>

    override val api: Set<FeatureWrapper<out Feature>> = setOf(
        FeatureWrapper(TestFeature2::class.java) {
            object : TestFeature2 {
                init {
                    testFeature1.get()
                }
            }
        }
    )

    override val dependency: Dependency = Dependency.Builder()
        .require(TestFeature1::class.java) { testFeature1 = it }
        .build()

    override val customizationOptions: Unit = Unit

}
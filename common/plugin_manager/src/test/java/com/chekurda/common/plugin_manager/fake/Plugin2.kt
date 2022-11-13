package com.chekurda.common.plugin_manager.fake

import com.chekurda.common.plugin_struct.BasePlugin
import com.chekurda.common.plugin_struct.Dependency
import com.chekurda.common.plugin_struct.Feature
import com.chekurda.common.plugin_struct.FeatureWrapper

class Plugin2 : BasePlugin<Unit>() {
    internal val testFeature2 = object : TestFeature2 {}

    override val api: Set<FeatureWrapper<out Feature>> = setOf(
        FeatureWrapper(TestFeature2::class.java) {
            testFeature2
        }
    )

    override val dependency: Dependency = Dependency.Builder()
        .build()

    override val customizationOptions: Unit = Unit

}
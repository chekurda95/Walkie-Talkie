package com.chekurda.walkie_talkie

import com.chekurda.common.plugin_struct.*
import com.chekurda.walkie_talkie.main_screen.contact.MainScreenFragmentFactory

object AppPlugin : BasePlugin<Unit>() {

    private lateinit var mainScreenFactoryFeature: FeatureProvider<MainScreenFragmentFactory>

    val mainScreenFragmentFactory: MainScreenFragmentFactory by lazy {
        mainScreenFactoryFeature.get()
    }

    override val api: Set<FeatureWrapper<out Feature>> = setOf()
    override val customizationOptions: Unit = Unit
    override val dependency: Dependency = Dependency.Builder()
        .require(MainScreenFragmentFactory::class.java) { mainScreenFactoryFeature = it }
        .build()
}
package com.chekurda.main_screen

import com.chekurda.common.plugin_struct.BasePlugin
import com.chekurda.common.plugin_struct.Dependency
import com.chekurda.common.plugin_struct.Feature
import com.chekurda.common.plugin_struct.FeatureWrapper
import com.chekurda.main_screen.contact.MainScreenFragmentFactory
import com.chekurda.main_screen.presentation.MainScreenFragment

object MainScreenPlugin : BasePlugin<Unit>() {

    override val api: Set<FeatureWrapper<out Feature>> = setOf(
        FeatureWrapper(MainScreenFragmentFactory::class.java) { MainScreenFragment.Companion }
    )

    override val dependency: Dependency = Dependency.EMPTY
    override val customizationOptions: Unit = Unit
}
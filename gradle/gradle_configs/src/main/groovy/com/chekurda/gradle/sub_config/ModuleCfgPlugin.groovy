package com.chekurda.gradle.sub_config

import org.gradle.api.Plugin
import org.gradle.api.Project

class ModuleCfgPlugin implements Plugin<Project> {
    void apply(Project project) {
        project.apply plugin: 'com.android.library'
        project.getPlugins().apply(BaseModuleCfgPlugin.class)
        project.getPlugins().apply(KotlinCfgPlugin.class)
    }
}
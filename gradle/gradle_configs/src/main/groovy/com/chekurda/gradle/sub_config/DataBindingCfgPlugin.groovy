package com.chekurda.gradle.sub_config

import org.gradle.api.Plugin
import org.gradle.api.Project

class DataBindingCfgPlugin implements Plugin<Project> {
    void apply(Project project) {
        project.android.buildFeatures {
            dataBinding = true
        }
    }
}
package com.chekurda.gradle.sub_config

import org.gradle.api.Plugin
import org.gradle.api.Project

class DaggerCfgPlugin implements Plugin<Project> {
    void apply(Project project) {
        project.dependencies {
            implementation "com.google.dagger:dagger:$project.rootProject.ext.daggerVersion"
            kapt "com.google.dagger:dagger-compiler:$project.rootProject.ext.daggerVersion"
        }
    }
}
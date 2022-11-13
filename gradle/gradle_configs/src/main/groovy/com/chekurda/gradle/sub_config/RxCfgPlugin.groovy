package com.chekurda.gradle.sub_config

import org.gradle.api.Plugin
import org.gradle.api.Project

class RxCfgPlugin implements Plugin<Project> {
    void apply(Project project) {
        project.dependencies {
            implementation "io.reactivex.rxjava2:rxjava:$project.rootProject.ext.rxVersion"
            implementation "io.reactivex.rxjava2:rxandroid:$project.rootProject.ext.rxAndroidVersion"
        }
    }
}
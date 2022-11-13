package com.chekurda.gradle.sub_config

import org.gradle.api.Plugin
import org.gradle.api.Project

class KotlinCfgPlugin implements Plugin<Project> {
    void apply(Project project) {
        project.apply plugin: 'kotlin-android'
        project.apply plugin: 'kotlin-kapt'

        project.dependencies {
            implementation "org.jetbrains.kotlin:kotlin-stdlib-jdk8:$project.rootProject.ext.versions.kotlin"
        }
    }
}
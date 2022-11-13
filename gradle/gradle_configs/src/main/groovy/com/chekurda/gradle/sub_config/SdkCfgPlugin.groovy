package com.chekurda.gradle.sub_config

import org.gradle.api.Plugin
import org.gradle.api.Project

class SdkCfgPlugin implements Plugin<Project> {
    void apply(Project project) {
        project.dependencies {
            api "com.google.android.material:material:$project.rootProject.ext.materialVersion"
            api "com.jakewharton.rxbinding2:rxbinding:$project.rootProject.ext.rxBindingVersion"
            api "androidx.annotation:annotation:$project.rootProject.ext.annotationVersion"
            api "org.apache.commons:commons-lang3:$project.rootProject.ext.apacheCommonsVersion"

            api "androidx.fragment:fragment:$project.rootProject.ext.fragmentVersion"
            api "androidx.fragment:fragment-ktx:$project.rootProject.ext.fragmentVersion"
            api "androidx.core:core-ktx:$project.rootProject.ext.androidCore"
            api "androidx.appcompat:appcompat:$project.rootProject.ext.appCompatVersion"
            api "androidx.lifecycle:lifecycle-viewmodel-ktx:$project.rootProject.ext.androidLifecycleViewmodel"
            api "androidx.lifecycle:lifecycle-livedata-ktx:$project.androidArchLifecycle"
            api "androidx.legacy:legacy-preference-v14:$project.rootProject.ext.androidSupportVersion"
            api "androidx.recyclerview:recyclerview:$project.rootProject.ext.recyclerViewVersion"
            api "androidx.constraintlayout:constraintlayout:$project.rootProject.ext.constraintLayoutVersion"
        }
    }
}
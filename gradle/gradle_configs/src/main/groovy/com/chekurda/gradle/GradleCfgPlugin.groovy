package com.chekurda.gradle

import com.chekurda.gradle.root_config.ApplicationCfgPlugin
import com.chekurda.gradle.sub_config.DataBindingCfgPlugin
import com.chekurda.gradle.sub_config.ModuleCfgPlugin
import com.chekurda.gradle.sub_config.DaggerCfgPlugin
import com.chekurda.gradle.sub_config.KotlinCfgPlugin
import com.chekurda.gradle.sub_config.BaseModuleCfgPlugin
import com.chekurda.gradle.sub_config.RxCfgPlugin
import com.chekurda.gradle.sub_config.SdkCfgPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project

import java.util.function.Function

/**
 * Плагин производит установку типовых плагинов для проекта.
 */
class GradleCfgPlugin implements Plugin<Project> {
    void apply(Project project) {
        project.ext.gradleConfig = { Function<GradleConfig, Void> config ->
            config.apply(new GradleConfig(project))
        }
    }

    static class GradleConfig {
        private def project

        private GradleConfig(Project project) {
            this.project = project
        }

        void enableApplicationCfg() {
            project.getPlugins().apply(ApplicationCfgPlugin.class)
        }

        void enableBaseModuleCfg() {
            project.getPlugins().apply(BaseModuleCfgPlugin.class)
        }

        void enableModuleCfg() {
            project.getPlugins().apply(ModuleCfgPlugin.class)
        }

        void enableSdkCfg() {
            project.getPlugins().apply(SdkCfgPlugin.class)
        }

        void enableKotlinCfg() {
            project.getPlugins().apply(KotlinCfgPlugin.class)
        }

        void enableRxCfg() {
            project.getPlugins().apply(RxCfgPlugin.class)
        }

        void enableDaggerCfg() {
            project.getPlugins().apply(DaggerCfgPlugin.class)
        }

        void enableDataBindingCfg() {
            project.getPlugins().apply(DataBindingCfgPlugin.class)
        }
    }
}
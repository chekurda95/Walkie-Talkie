package com.chekurda.common.plugin_struct

import android.app.Application

/**
 * Базовая реализация плагина, главное назначение которой сохранение экземпляра приложения.
 */
abstract class BasePlugin<C> : Plugin<C> {
    lateinit var application: Application
        private set

    final override fun FeatureRegistry.setApplication(application: Application) {
        this@BasePlugin.application = application
    }
}
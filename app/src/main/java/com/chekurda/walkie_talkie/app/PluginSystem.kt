package com.chekurda.walkie_talkie.app

import android.app.Application
import com.chekurda.common.plugin_manager.PluginManager
import com.chekurda.common.plugin_struct.Plugin
import com.chekurda.main_screen.MainScreenPlugin
import com.chekurda.walkie_talkie.AppPlugin

/**
 * Система плагинов приложения.
 */
object PluginSystem {

    private val plugins: Array<Plugin<*>>
        get() = arrayOf(
            AppPlugin,
            MainScreenPlugin
        )

    /**
     * Метод для инициализации плагинной системы.
     */
    fun initialize(
        app: Application,
        pluginManager: PluginManager = PluginManager()
    ) {
        pluginManager.registerPlugins(*plugins)
        pluginManager.configure(app)
    }
}
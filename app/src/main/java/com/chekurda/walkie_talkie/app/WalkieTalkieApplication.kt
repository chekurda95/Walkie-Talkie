package com.chekurda.walkie_talkie.app

import android.app.Application

/**
 * [Application] Walkie-Talkie.
 */
class WalkieTalkieApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        PluginSystem.initialize(this)
    }
}
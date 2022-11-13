package com.chekurda.main_screen.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter

internal class SimpleReceiver(
    action: String,
    private val isSingleEvent: Boolean = false,
    private val onReceive: (Intent) -> Unit
) : BroadcastReceiver() {

    private val intentFilter = IntentFilter(action)
    private var isRegistered = false

    override fun onReceive(context: Context, intent: Intent) {
        onReceive(intent)
        if (isSingleEvent) unregister(context)
    }

    fun register(context: Context) {
        if (isRegistered) return
        context.registerReceiver(this, intentFilter)
        isRegistered = true
    }

    fun unregister(context: Context) {
        if (!isRegistered) return
        try {
            context.unregisterReceiver(this)
        } catch (ignore: IllegalArgumentException) {}
        isRegistered = false
    }
}
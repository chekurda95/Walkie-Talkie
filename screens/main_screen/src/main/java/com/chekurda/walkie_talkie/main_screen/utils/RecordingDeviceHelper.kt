package com.chekurda.walkie_talkie.main_screen.utils

import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.media.AudioManager
import android.os.PowerManager

/**
 * Вспомогательный класс для управления девайсом в процессе записи аудио.
 */
internal class RecordingDeviceHelper(private val activity: Activity) {

    private val systemAudioManager = activity.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val powerManager = activity.getSystemService(Context.POWER_SERVICE) as PowerManager

    private var wakeLock: PowerManager.WakeLock? = null
    private var hasRecordAudioFocus = false
    private val audioRecordFocusChangedListener = AudioManager.OnAudioFocusChangeListener { focus ->
        if (focus != AudioManager.AUDIOFOCUS_GAIN) hasRecordAudioFocus = false
    }

    private var isConfigured: Boolean = false

    /**
     * Настроить девайс для записи аудио/видео.
     * В настройке участвуют следующие параметры:
     * - Блокируется ориентация экрана.
     * - Производится попытка переключения на блютуз аудио-вход.
     * - Останавливаются текущие проигрываемые аудио/видео на девайсе.
     * - Блокируется погасание экрана.
     *
     * @param isStartRecording true, если необходима настройка перед записью,
     * в ином случае настройки вернутся к прежним параметрам.
     */
    fun configureDevice(isStartRecording: Boolean) {
        if (isConfigured && isStartRecording || !(isConfigured || isStartRecording)) return
        isConfigured = isStartRecording
        requestLockOrientation(isStartRecording)
        requestBluetoothSco(isStartRecording)
        requestAudioFocus(isStartRecording)
        requestWakeLock(isStartRecording)
    }

    private fun requestLockOrientation(request: Boolean) {
        activity.requestedOrientation = if (request) {
            ActivityInfo.SCREEN_ORIENTATION_LOCKED
        } else {
            ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    private fun requestBluetoothSco(request: Boolean) = with(systemAudioManager) {
        if (request == isBluetoothScoOn) return
        if (request) {
            try {
                startBluetoothSco()
                isBluetoothScoOn = request
            } catch (ignore: RuntimeException) { }
        } else {
            stopBluetoothSco()
        }
    }

    @Suppress("DEPRECATION")
    private fun requestAudioFocus(request: Boolean) {
        if (request && !hasRecordAudioFocus) {
            systemAudioManager.requestAudioFocus(
                audioRecordFocusChangedListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT
            )
            hasRecordAudioFocus = true
        } else if (!request && hasRecordAudioFocus) {
            systemAudioManager.abandonAudioFocus(audioRecordFocusChangedListener)
            hasRecordAudioFocus = false
        }
    }

    private fun requestWakeLock(request: Boolean) {
        try {
            if (request && wakeLock == null) {
                wakeLock = powerManager.newWakeLock(
                    PowerManager.SCREEN_DIM_WAKE_LOCK or PowerManager.ON_AFTER_RELEASE,
                    AUDIO_RECORD_WAKELOCK_TAG
                ).also { it.acquire(WAKELOCK_TIMEOUT_MS) }
            } else {
                wakeLock?.also {
                    it.release()
                    wakeLock = null
                }
            }
        } catch (ignore: Exception) { }
    }
}

private const val WAKELOCK_TIMEOUT_MS = 5000 * 60L
private const val AUDIO_RECORD_WAKELOCK_TAG = "com.chekurda.walkie_talkie.main_screen.utils:AUDIO_RECORD_WAKELOCK"
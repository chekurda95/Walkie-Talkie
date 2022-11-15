package com.chekurda.walkie_talkie.main_screen.utils

import android.app.Activity
import android.content.Context
import android.media.AudioManager

/**
 * Вспомогательный класс для управления девайсом в процессе записи аудио.
 */
internal class RecordingDeviceHelper(activity: Activity) {

    private val systemAudioManager = activity.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    private var hasRecordAudioFocus = false
    private val audioRecordFocusChangedListener = AudioManager.OnAudioFocusChangeListener { focus ->
        if (focus != AudioManager.AUDIOFOCUS_GAIN) hasRecordAudioFocus = false
    }

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
        requestBluetoothSco(isStartRecording)
        requestAudioFocus(isStartRecording)
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
}
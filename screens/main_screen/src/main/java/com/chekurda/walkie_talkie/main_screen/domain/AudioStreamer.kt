package com.chekurda.walkie_talkie.main_screen.domain

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.os.Build
import java.net.Socket

internal class AudioStreamer {

    private var track: AudioTrack? = null
    private var recorder: AudioRecord? = null

    private val minBufferSize = AudioTrack.getMinBufferSize(
        SAMPLE_RATE,
        AudioFormat.CHANNEL_OUT_MONO,
        ENCODING_TYPE
    )
    @Volatile
    private var isConnected: Boolean = false
    @Volatile
    private var isListening: Boolean = true

    fun changeStreamDirection(isListening: Boolean) {
        this.isListening = isListening
    }

    fun connect(socket: Socket, onDisconnect: () -> Unit) {
        if (isConnected) return
        isListening = true
        track = prepareAudioTrack().apply { play() }
        recorder = prepareAudioRecorder().apply { startRecording() }
        createAudioPlayerThread(socket, onDisconnect).start()
        createAudioRecordThread(socket, onDisconnect).start()
        isConnected = true
    }

    fun disconnect() {
        isConnected = false
        isListening = true
    }

    private fun prepareAudioTrack(): AudioTrack {
        val attrs = AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
            .build()
        val format = AudioFormat.Builder()
            .setSampleRate(SAMPLE_RATE)
            .setEncoding(ENCODING_TYPE)
            .setChannelMask(AudioFormat.CHANNEL_OUT_FRONT_LEFT)
            .build()

        return AudioTrack.Builder()
            .setAudioAttributes(attrs)
            .setAudioFormat(format)
            .setBufferSizeInBytes(minBufferSize)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    setPerformanceMode(AudioTrack.PERFORMANCE_MODE_POWER_SAVING)
                }
            }
            .build()
    }

    private fun prepareAudioRecorder(): AudioRecord =
        AudioRecord(
            MediaRecorder.AudioSource.MIC,
            SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            ENCODING_TYPE,
            minBufferSize
        )

    private fun createAudioPlayerThread(socket: Socket, onDisconnect: () -> Unit): Thread =
        object : Thread() {
            override fun run() {
                super.run()
                val buffer = ByteArray(minBufferSize)
                val inputStream = socket.getInputStream()
                kotlin.runCatching {
                    while (isConnected) {
                        if (inputStream.available() == 0) continue
                        if (isListening) {
                            inputStream.read(buffer)
                            track?.write(buffer, 0, buffer.size)
                        }
                    }
                }.apply { if (isFailure) onDisconnect() }
                isConnected = false
                socket.close()
            }
        }.apply { priority = Thread.MAX_PRIORITY }

    private fun createAudioRecordThread(socket: Socket, onDisconnect: () -> Unit): Thread =
        object : Thread() {
            override fun run() {
                super.run()
                val buffer = ByteArray(minBufferSize)
                val outputStream = socket.getOutputStream()
                kotlin.runCatching {
                    while (isConnected) {
                        if (!isListening) {
                            recorder!!.read(buffer, 0, buffer.size)
                            outputStream.write(buffer)
                        }
                    }
                }.apply { if (isFailure) onDisconnect() }
                isConnected = false
                socket.close()
            }
        }.apply { priority = Thread.MAX_PRIORITY }
}

private const val SAMPLE_RATE = 16000
private const val ENCODING_TYPE = AudioFormat.ENCODING_PCM_16BIT
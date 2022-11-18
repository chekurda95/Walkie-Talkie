package com.chekurda.walkie_talkie.main_screen.domain

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import androidx.annotation.WorkerThread
import java.net.Socket
import java.nio.BufferUnderflowException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.sqrt

internal class AudioStreamer {

    interface AmplitudeListener {
        @WorkerThread
        fun onInputAmplitudeChanged(amplitude: Float)
        @WorkerThread
        fun onOutputAmplitudeChanged(amplitude: Float)
    }

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

    var amplitudeListener: AmplitudeListener? = null

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
        kotlin.runCatching {
            recorder?.release()
            recorder = null
        }
        kotlin.runCatching {
            track?.release()
            track = null
        }
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
                val byteArray = ByteArray(minBufferSize)
                kotlin.runCatching {
                    val inputStream = socket.getInputStream()
                    var lastAmplitude = 0f
                    while (isConnected) {
                        if (inputStream.available() == 0 || !isListening) {
                            if (lastAmplitude != 0f) {
                                lastAmplitude = 0f
                                amplitudeListener?.onInputAmplitudeChanged(0f)
                            }
                        } else {
                            inputStream.read(byteArray)
                            track?.write(byteArray, 0, byteArray.size)
                            val amplitude = getAmplitude(byteArray, byteArray.size)
                            if (lastAmplitude != amplitude) {
                                lastAmplitude = amplitude
                                amplitudeListener?.onInputAmplitudeChanged(amplitude)
                            }
                        }
                    }
                }.apply {
                    if (isFailure) {
                        Log.e("AudioStreamer", "audioPlayerThread ${exceptionOrNull()}")
                        onDisconnect()
                    }
                }
                isConnected = false
                socket.close()
            }
        }.apply { priority = Thread.MAX_PRIORITY }

    private fun createAudioRecordThread(socket: Socket, onDisconnect: () -> Unit): Thread =
        object : Thread() {
            override fun run() {
                super.run()
                val byteArray = ByteArray(minBufferSize)
                kotlin.runCatching {
                    val outputStream = socket.getOutputStream()
                    var lastAmplitude = 0f
                    while (isConnected) {
                        if (!isListening) {
                            recorder!!.read(byteArray, 0, byteArray.size)
                            outputStream.write(byteArray)
                            val amplitude = getAmplitude(byteArray, byteArray.size)
                            if (lastAmplitude != amplitude) {
                                lastAmplitude = amplitude
                                amplitudeListener?.onOutputAmplitudeChanged(amplitude)
                            }
                        }
                    }
                }.apply {
                    if (isFailure) {
                        Log.e("AudioStreamer", "audioRecordThread ${exceptionOrNull()}")
                        onDisconnect()
                    }
                }
                isConnected = false
                socket.close()
            }
        }.apply { priority = Thread.MAX_PRIORITY }

    private fun getAmplitude(byteArray: ByteArray, length: Int): Float {
        val buffer = ByteBuffer.wrap(byteArray).apply { order(ByteOrder.nativeOrder()) }
        var sum = 0.0f
        try {
            repeat(length / 2) {
                val peak = buffer.short
                sum += peak * peak
            }
        } catch (ignore: BufferUnderflowException) {}

        return sqrt(sum / length / 2) / AMPLITUDE_SCALE
    }
}

private const val SAMPLE_RATE = 16000
private const val ENCODING_TYPE = AudioFormat.ENCODING_PCM_16BIT

/**
 * Масштаб амплитуды громкости для кнопки записи.
 */
private const val AMPLITUDE_SCALE = 1800
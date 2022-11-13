package com.chekurda.common.surface

import android.util.Log
import androidx.annotation.IntRange

internal class FPSHelper(
    @IntRange(from = ONE_FPS, to = MAX_FPS)
    private val fps: Int
) {

    private val frameTimeMs = 1000 / fps

    private var previousDrawTimeMs = 0L

    val isTimeToDraw: Boolean
        get() {
            val currentTimeMs = System.currentTimeMillis()
            val timeDelta = currentTimeMs - previousDrawTimeMs
            return (timeDelta >= frameTimeMs).also { isTimeToDraw ->
                if (isTimeToDraw) {
                    previousDrawTimeMs = currentTimeMs
                    if (timeDelta >= frameTimeMs * 2) {
                        Log.w("FPSHelper", "Осторожно, обнаружен пропуск кадров, установленное значение FPS = $fps")
                    }
                }
            }
        }
}

internal const val ONE_FPS = 1L
internal const val MAX_FPS = 60L
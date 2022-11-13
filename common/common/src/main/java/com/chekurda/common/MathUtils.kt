package com.chekurda.common

import kotlin.math.roundToInt

val Int.half: Int
    get() = (this / 2.0).roundToInt()

val Float.half: Float
    get() = this / 2
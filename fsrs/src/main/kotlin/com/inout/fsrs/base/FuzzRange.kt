package com.inout.fsrs.base

import kotlin.math.roundToInt

val FUZZ_RANGES = listOf(
    FuzzRange(2.5, 7.0, 0.15),
    FuzzRange(7.0, 20.0, 0.1),
    FuzzRange(20.0, Double.POSITIVE_INFINITY, 0.05)
)

data class FuzzRange(val start: Double, val end: Double, val factor: Double)

fun getFuzzRange(interval: Double, elapsedDays: Int, maximumInterval: Int): Pair<Int, Int> {
    var delta = 1.0
    for (range in FUZZ_RANGES) {
        delta += range.factor * maxOf(minOf(interval, range.end) - range.start, 0.0)
    }
    var minIvl = maxOf(2, (interval - delta).roundToInt())
    val maxIvl = minOf((interval + delta).roundToInt(), maximumInterval)
    if (interval > elapsedDays) {
        minIvl = maxOf(minIvl, elapsedDays + 1)
    }
    minIvl = minOf(minIvl, maxIvl)
    return minIvl to maxIvl
}
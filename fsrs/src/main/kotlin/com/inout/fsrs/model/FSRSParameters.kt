package com.inout.fsrs.model

const val DEFAULT_REQUEST_RETENTION = 0.9
const val DEFAULT_MAX_INTERVAL = 36500
val DEFAULT_WEIGHT = listOf(
    0.5701, 1.4436, 4.1386, 10.9355, 5.1443, 1.2006, 0.8627, 0.0362, 1.629,
    0.1342, 1.0166, 2.1174, 0.0839, 0.3204, 1.4676, 0.219, 2.8237
)
const val DEFAULT_ENABLE_FUZZ = false

data class FSRSParameters(
    val requestRetention: Double = DEFAULT_REQUEST_RETENTION,
    val maximumInterval: Int = DEFAULT_MAX_INTERVAL,
    val weights: List<Double> = DEFAULT_WEIGHT,
    val enableFuzz: Boolean = DEFAULT_ENABLE_FUZZ
)

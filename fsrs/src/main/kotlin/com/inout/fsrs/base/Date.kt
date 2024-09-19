package com.inout.fsrs.base

import java.time.Instant
import java.util.*

fun Date.addDays(
    days: Int,
    fuzz: Boolean = false,
): Date {
    val calendar = Calendar.getInstance()
    calendar.time = this
    calendar.add(Calendar.DAY_OF_YEAR, days + if (fuzz) (0..5).random() else 0)
    return calendar.time
}

fun Date.addMinutes(minutes: Int): Date {
    val calendar = Calendar.getInstance()
    calendar.time = this
    calendar.add(Calendar.MINUTE, minutes)
    return calendar.time
}

/**
 * Calculate the difference in days between two dates.
 * if difference is 0.9 days, it will be rounded to 0.
 */
fun Date.diffInDays(other: Date): Int {
    val diffInMills = this.time - other.time
    return (diffInMills / (1000 * 60 * 60 * 24)).toInt()
}

fun Instant.diffInDays(other: Instant): Int {
    return diffInMillis(other).toInt() / (1000 * 60 * 60 * 24)
}

fun Instant.diffInMillis(other: Instant): Long {
    return this.toEpochMilli() - other.toEpochMilli()
}

fun Instant.plusMinutes(minutes: Int): Instant {
    return this.plusSeconds(minutes * TimeConstant.MINUTE_TO_SECONDS)
}

fun Instant.plusHours(hours: Int): Instant {
    return this.plusSeconds(hours * TimeConstant.HOUR_TO_SECONDS)
}

fun Instant.plusDays(days: Int): Instant {
    return this.plusSeconds(days * TimeConstant.DAY_TO_SECONDS)
}

object TimeConstant {
    const val MINUTE_TO_SECONDS = 60L
    const val HOUR_TO_SECONDS = MINUTE_TO_SECONDS * 60
    const val DAY_TO_SECONDS = HOUR_TO_SECONDS * 24
    const val WEEK_TO_SECONDS = DAY_TO_SECONDS * 7
}

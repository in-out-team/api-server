package com.inout.fsrs.base

import java.util.*

fun Date.addDays(days: Int, fuzz: Boolean = false): Date {
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
fun Date.diff(other: Date): Int {
    val diffInMills = this.time - other.time
    return (diffInMills / (1000 * 60 * 60 * 24)).toInt()
}

package com.inout.fsrs.model

import com.inout.fsrs.model.enums.Rating
import com.inout.fsrs.model.enums.State
import java.time.Instant

data class ReviewLog(
    val rating: Rating,
    val state: State,
    val due: Instant,
    val stability: Double,
    val difficulty: Double,
    val elapsedDays: Int,
    val lastElapsedDays: Int,
    val scheduledDays: Int,
    val review: Instant,
)

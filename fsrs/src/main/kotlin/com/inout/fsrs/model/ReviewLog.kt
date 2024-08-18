package com.inout.fsrs.model

import com.inout.fsrs.model.enums.Rating
import com.inout.fsrs.model.enums.State
import java.util.*

data class ReviewLog(
    val rating: Rating,
    val state: State,
    val due: Date,
    val stability: Double,
    val difficulty: Double,
    val elapsedDays: Int,
    val lastElapsedDays: Int,
    val scheduledDays: Int,
    val review: Date,
)

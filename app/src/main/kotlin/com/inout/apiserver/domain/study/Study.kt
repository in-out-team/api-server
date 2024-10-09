package com.inout.apiserver.domain.study

import com.inout.apiserver.base.enums.FsrsCardState
import com.inout.fsrs.model.Card
import java.time.Instant

data class Study(
    val id: Long,
    val userId: Long,
    // should be unmodifiable
    val wordDefinitionId: Long,
    val state: FsrsCardState,
    val due: Instant,
    val stability: Double,
    val difficulty: Double,
    val elapsedDays: Int,
    val scheduledDays: Int,
    val reps: Int,
    val lapses: Int,
    val lastReview: Instant?,
    val reviewLogs: List<StudyReviewLog>,
    val createdAt: Instant?,
    val updatedAt: Instant?,
) {
    fun toFsrsCard(): Card =
        Card(
            state = FsrsCardState.toFsrsState(state),
            due = due,
            stability = stability,
            difficulty = difficulty,
            elapsedDays = elapsedDays,
            scheduledDays = scheduledDays,
            reps = reps,
            lapses = lapses,
            lastReview = lastReview,
        )
}

package com.inout.apiserver.domain.study

import com.inout.apiserver.base.enums.FsrsCardState
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
    val createdAt: Instant?,
    val updatedAt: Instant?,
)

package com.inout.apiserver.domain.study

import com.inout.apiserver.base.enums.FsrsCardRating
import com.inout.apiserver.base.enums.FsrsCardState
import java.time.Instant

data class StudyReviewLogCreateObject(
    val rating: FsrsCardRating,
    val state: FsrsCardState,
    val due: Instant,
    val stability: Double,
    val difficulty: Double,
    val elapsedDays: Int,
    val lastElapsedDays: Int,
    val scheduledDays: Int,
    val review: Instant,
)

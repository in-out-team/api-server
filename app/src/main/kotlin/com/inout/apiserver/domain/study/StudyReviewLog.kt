package com.inout.apiserver.domain.study

import com.inout.apiserver.base.enums.FsrsCardRating
import com.inout.apiserver.base.enums.FsrsCardState
import com.inout.fsrs.model.ReviewLog
import java.time.Instant

data class StudyReviewLog(
    val id: Long,
    val rating: FsrsCardRating,
    val state: FsrsCardState,
    val due: Instant,
    val stability: Double,
    val difficulty: Double,
    val elapsedDays: Int,
    val lastElapsedDays: Int,
    val scheduledDays: Int,
    val review: Instant,
) {
    companion object {
        fun newFrom(fsrsReviewLog: ReviewLog): StudyReviewLog {
            return StudyReviewLog(
                id = -1,
                rating = FsrsCardRating.of(fsrsReviewLog.rating),
                state = FsrsCardState.of(fsrsReviewLog.state),
                due = fsrsReviewLog.due,
                stability = fsrsReviewLog.stability,
                difficulty = fsrsReviewLog.difficulty,
                elapsedDays = fsrsReviewLog.elapsedDays,
                lastElapsedDays = fsrsReviewLog.lastElapsedDays,
                scheduledDays = fsrsReviewLog.scheduledDays,
                review = fsrsReviewLog.review,
            )
        }
    }
}

package com.inout.apiserver.infrastructure.db.study

import com.inout.apiserver.base.alias.StudyReviewLogId
import com.inout.apiserver.base.enums.FsrsCardRating
import com.inout.apiserver.base.enums.FsrsCardState
import com.inout.apiserver.domain.study.StudyReviewLogCreateObject
import com.inout.apiserver.infrastructure.db.TimestampedEntity
import com.inout.fsrs.model.ReviewLog
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "study_review_logs")
data class StudyReviewLog(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: StudyReviewLogId? = null,
    @Enumerated(EnumType.STRING)
    val rating: FsrsCardRating,
    @Enumerated(EnumType.STRING)
    val state: FsrsCardState,
    val due: Instant,
    val stability: Double,
    val difficulty: Double,
    val elapsedDays: Int,
    val lastElapsedDays: Int,
    val scheduledDays: Int,
    val review: Instant,
) : TimestampedEntity() {
    companion object {
        fun fromCreateObject(createObject: StudyReviewLogCreateObject): StudyReviewLog =
            StudyReviewLog(
                rating = createObject.rating,
                state = createObject.state,
                due = createObject.due,
                stability = createObject.stability,
                difficulty = createObject.difficulty,
                elapsedDays = createObject.elapsedDays,
                lastElapsedDays = createObject.lastElapsedDays,
                scheduledDays = createObject.scheduledDays,
                review = createObject.review,
            )

        fun fromFsrsReviewLog(fsrsReviewLog: ReviewLog): StudyReviewLog =
            StudyReviewLog(
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

package com.inout.apiserver.infrastructure.db.study

import com.inout.apiserver.base.enums.FsrsCardRating
import com.inout.apiserver.base.enums.FsrsCardState
import com.inout.apiserver.domain.study.StudyReviewLog
import com.inout.apiserver.domain.study.StudyReviewLogCreateObject
import com.inout.apiserver.error.InOutRequireNotNullException
import com.inout.apiserver.infrastructure.db.BaseEntity
import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "study_review_logs")
data class StudyReviewLogEntity(
    val rating: FsrsCardRating,
    val state: FsrsCardState,
    val due: Instant,
    val stability: Double,
    val difficulty: Double,
    val elapsedDays: Int,
    val lastElapsedDays: Int,
    val scheduledDays: Int,
    val review: Instant,
) : BaseEntity() {
    fun toDomain(): StudyReviewLog {
        return StudyReviewLog(
            id = id ?: throw InOutRequireNotNullException("StudyReviewLog id is null", "IORNN_STUDYREVIEWLOG_1"),
            rating = rating,
            state = state,
            due = due,
            stability = stability,
            difficulty = difficulty,
            elapsedDays = elapsedDays,
            lastElapsedDays = lastElapsedDays,
            scheduledDays = scheduledDays,
            review = review,
        )
    }

    companion object {
        fun of(studyReviewLog: StudyReviewLog): StudyReviewLogEntity {
            return StudyReviewLogEntity(
                rating = studyReviewLog.rating,
                state = studyReviewLog.state,
                due = studyReviewLog.due,
                stability = studyReviewLog.stability,
                difficulty = studyReviewLog.difficulty,
                elapsedDays = studyReviewLog.elapsedDays,
                lastElapsedDays = studyReviewLog.lastElapsedDays,
                scheduledDays = studyReviewLog.scheduledDays,
                review = studyReviewLog.review,
            ).apply {
                id = studyReviewLog.id
                // TODO: check if this makes any unexpected changes on createdAt/updatedAt
            }
        }

        fun fromCreateObject(studyReviewLogCreateObject: StudyReviewLogCreateObject): StudyReviewLogEntity {
            return StudyReviewLogEntity(
                rating = studyReviewLogCreateObject.rating,
                state = studyReviewLogCreateObject.state,
                due = studyReviewLogCreateObject.due,
                stability = studyReviewLogCreateObject.stability,
                difficulty = studyReviewLogCreateObject.difficulty,
                elapsedDays = studyReviewLogCreateObject.elapsedDays,
                lastElapsedDays = studyReviewLogCreateObject.lastElapsedDays,
                scheduledDays = studyReviewLogCreateObject.scheduledDays,
                review = studyReviewLogCreateObject.review,
            )
        }
    }
}

package com.inout.apiserver.domain.study

import com.inout.apiserver.base.enums.FsrsCardState
import com.inout.apiserver.infrastructure.db.study.StudyEntity
import com.inout.apiserver.infrastructure.db.study.StudyRepository
import com.inout.apiserver.infrastructure.db.study.StudyReviewLogEntity
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class StudyFactory(
    private val studyRepository: StudyRepository,
) {
    fun createStudy(
        userId: Long = 1L,
        wordDefinitionId: Long = 1L,
        state: FsrsCardState = FsrsCardState.NEW,
        due: Instant = Instant.now(),
        stability: Double = 0.0,
        difficulty: Double = 0.0,
        elapsedDays: Int = 0,
        scheduledDays: Int = 0,
        reps: Int = 0,
        lapses: Int = 0,
        lastReview: Instant? = null,
        reviewLogs: List<StudyReviewLog> = emptyList(),
    ): Study =
        studyRepository.save(
            StudyEntity(
                userId = userId,
                wordDefinitionId = wordDefinitionId,
                state = state,
                due = due,
                stability = stability,
                difficulty = difficulty,
                elapsedDays = elapsedDays,
                scheduledDays = scheduledDays,
                reps = reps,
                lapses = lapses,
                lastReview = lastReview,
                reviewLogs = reviewLogs.map { StudyReviewLogEntity.fromDomain(it) },
            ),
        )

    companion object {
        @Deprecated("remove later...")
        fun createStudy(
            id: Long = 1L,
            userId: Long = 1L,
            wordDefinitionId: Long = 1L,
            state: FsrsCardState = FsrsCardState.NEW,
            due: Instant = Instant.now(),
            stability: Double = 0.0,
            difficulty: Double = 0.0,
            elapsedDays: Int = 0,
            scheduledDays: Int = 0,
            reps: Int = 0,
            lapses: Int = 0,
            lastReview: Instant? = null,
            reviewLogs: List<StudyReviewLog> = emptyList(),
        ): Study {
            val now = Instant.now()
            return Study(
                id = id,
                userId = userId,
                wordDefinitionId = wordDefinitionId,
                state = state,
                due = due,
                stability = stability,
                difficulty = difficulty,
                elapsedDays = elapsedDays,
                scheduledDays = scheduledDays,
                reps = reps,
                lapses = lapses,
                lastReview = lastReview,
                reviewLogs = reviewLogs,
                createdAt = now,
                updatedAt = now,
            )
        }
    }
}

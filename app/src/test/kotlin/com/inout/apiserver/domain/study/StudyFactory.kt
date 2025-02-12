package com.inout.apiserver.domain.study

import com.inout.apiserver.base.alias.UserId
import com.inout.apiserver.base.alias.WordDefinitionId
import com.inout.apiserver.base.enums.FsrsCardState
import com.inout.apiserver.infrastructure.db.study.DailyStudySetEntity
import com.inout.apiserver.infrastructure.db.study.DailyStudySetRepository
import com.inout.apiserver.infrastructure.db.study.Study
import com.inout.apiserver.infrastructure.db.study.StudyRepository
import com.inout.apiserver.infrastructure.db.study.StudyReviewLog
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.LocalDate

@Component
class StudyFactory(
    private val studyRepository: StudyRepository,
    private val dailyStudySetRepository: DailyStudySetRepository,
) {
    fun createStudy(
        userId: UserId = 1L,
        wordDefinitionId: WordDefinitionId = 1L,
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
            Study(
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
            ),
        )

    fun createDailyStudySet(
        userId: UserId,
        date: LocalDate = LocalDate.now(),
        studies: List<Study> = emptyList(),
    ) = dailyStudySetRepository.save(
        DailyStudySetEntity
            .fromCreateObject(
                DailyStudySetCreateObject(
                    userId = userId,
                    date = date,
                ),
            ).copy(
                studyIds = studies.map { it.id!! },
            ),
    )
}

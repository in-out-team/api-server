package com.inout.apiserver.domain.study

import com.inout.apiserver.base.enums.FsrsCardState
import com.inout.apiserver.infrastructure.mongo.study.DailyStudySet
import com.inout.apiserver.infrastructure.mongo.study.DailyStudySetRepository
import com.inout.apiserver.infrastructure.mongo.study.Study
import com.inout.apiserver.infrastructure.mongo.study.StudyRepository
import com.inout.apiserver.infrastructure.mongo.study.StudyReviewLog
import com.inout.apiserver.infrastructure.mongo.user.User
import org.bson.types.ObjectId
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.LocalDate

@Component
class StudyFactory(
    private val studyRepository: StudyRepository,
    private val dailyStudySetRepository: DailyStudySetRepository,
) {
    fun createStudy(
        userId: ObjectId,
        wordDefinitionId: ObjectId,
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
        studyRepository
            .save(
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
            ).let {
                studyRepository.findById(it.id!!).get()
            }

    fun createDailyStudySet(
        user: User,
        date: LocalDate? = null,
        studies: List<Study> = emptyList(),
    ): DailyStudySet =
        dailyStudySetRepository
            .save(
                DailyStudySet(
                    userId = user.id!!,
                    date = date ?: LocalDate.now(),
                    studyIds = studies.map { it.id!! },
                ),
            ).let {
                dailyStudySetRepository.findById(it.id!!).get()
            }
}

package com.inout.apiserver.domain.study

import com.inout.apiserver.base.enums.FsrsCardState
import com.inout.apiserver.infrastructure.mongo.study.MongoDailyStudySet
import com.inout.apiserver.infrastructure.mongo.study.MongoDailyStudySetRepository
import com.inout.apiserver.infrastructure.mongo.study.MongoStudy
import com.inout.apiserver.infrastructure.mongo.study.MongoStudyRepository
import com.inout.apiserver.infrastructure.mongo.study.MongoStudyReviewLog
import com.inout.apiserver.infrastructure.mongo.user.MongoUser
import org.bson.types.ObjectId
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.LocalDate

@Component
class MongoStudyFactory(
    private val studyRepository: MongoStudyRepository,
    private val dailyStudySetRepository: MongoDailyStudySetRepository,
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
        reviewLogs: List<MongoStudyReviewLog> = emptyList(),
    ): MongoStudy =
        studyRepository
            .save(
                MongoStudy(
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
        user: MongoUser,
        date: LocalDate? = null,
        studies: List<MongoStudy> = emptyList(),
    ): MongoDailyStudySet =
        dailyStudySetRepository
            .save(
                MongoDailyStudySet(
                    userId = user.id!!,
                    date = date ?: LocalDate.now(),
                    studyIds = studies.map { it.id!! },
                ),
            )
}

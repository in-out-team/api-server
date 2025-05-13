package com.inout.apiserver.infrastructure.mongo.study

import com.inout.apiserver.base.enums.FsrsCardRating
import com.inout.apiserver.base.enums.FsrsCardState
import com.inout.apiserver.domain.study.StudyReviewLogCreateObject
import com.inout.apiserver.error.InternalServerErrorException
import com.inout.fsrs.FSRS
import com.inout.fsrs.model.Card
import com.inout.fsrs.model.FSRSParameters
import com.inout.fsrs.model.ReviewLog
import com.inout.fsrs.model.enums.Grade
import org.bson.types.ObjectId
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.index.CompoundIndexes
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

data class MongoStudyReviewLog(
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
        fun fromCreateObject(createObject: StudyReviewLogCreateObject): MongoStudyReviewLog =
            MongoStudyReviewLog(
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

        fun fromFsrsReviewLog(fsrsReviewLog: ReviewLog): MongoStudyReviewLog =
            MongoStudyReviewLog(
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

@Document(collection = "studies")
@CompoundIndexes(
    CompoundIndex(
        name = "unique_userId_wordDefinitionId",
        def = "{'userId': 1, 'wordDefinitionId': 1}",
        unique = true,
    ),
)
data class MongoStudy(
    @Id
    val id: ObjectId? = null,
    val userId: ObjectId,
    val wordDefinitionId: ObjectId,
    val state: FsrsCardState,
    val due: Instant,
    val stability: Double,
    val difficulty: Double,
    val elapsedDays: Int,
    val scheduledDays: Int,
    val reps: Int,
    val lapses: Int,
    val lastReview: Instant? = null,
    val reviewLogs: List<MongoStudyReviewLog> = emptyList(),
    @CreatedDate
    val createdAt: Instant? = null,
    @LastModifiedDate
    val updatedAt: Instant? = null,
) {
    companion object {
        // FIXME: temporary
        fun fromCreateObject(
            userId: ObjectId,
            wordDefinitionId: ObjectId,
        ): MongoStudy {
            val fsrsCard = Card.createEmptyCard()
            return MongoStudy(
                userId = userId,
                wordDefinitionId = wordDefinitionId,
                state = FsrsCardState.of(fsrsCard.state),
                due = fsrsCard.due,
                stability = fsrsCard.stability,
                difficulty = fsrsCard.difficulty,
                elapsedDays = fsrsCard.elapsedDays,
                scheduledDays = fsrsCard.scheduledDays,
                reps = fsrsCard.reps,
                lapses = fsrsCard.lapses,
                lastReview = fsrsCard.lastReview,
                reviewLogs = emptyList(),
            )
        }

        val validSorts = setOf("createdAt", "due", "lastReview")
    }

    fun toFsrsCard() =
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

    fun rate(rating: FsrsCardRating): MongoStudy {
        val fsrsParam = FSRSParameters()
        val fsrs = FSRS(fsrsParam)
        val fsrsCard = toFsrsCard()
        val recordLog = fsrs.repeat(fsrsCard, due)
        val newRecordLogItem =
            recordLog.logs[Grade.fromRating(FsrsCardRating.toFsrsRating(rating))]
                ?: throw InternalServerErrorException(message = "FSRS Repeat Error", code = "STUDY_6")
        val updatedFsrsCard = newRecordLogItem.card
        val newFsrsReviewLog = newRecordLogItem.log

        return copy(
            state = FsrsCardState.of(updatedFsrsCard.state),
            due = updatedFsrsCard.due,
            stability = updatedFsrsCard.stability,
            difficulty = updatedFsrsCard.difficulty,
            elapsedDays = updatedFsrsCard.elapsedDays,
            scheduledDays = updatedFsrsCard.scheduledDays,
            reps = updatedFsrsCard.reps,
            lapses = updatedFsrsCard.lapses,
            lastReview = updatedFsrsCard.lastReview,
            reviewLogs = reviewLogs + MongoStudyReviewLog.fromFsrsReviewLog(newFsrsReviewLog),
        )
    }
}

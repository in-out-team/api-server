package com.inout.apiserver.infrastructure.db.study

import com.google.common.collect.Iterables
import com.inout.apiserver.base.alias.StudyId
import com.inout.apiserver.base.alias.UserId
import com.inout.apiserver.base.alias.WordDefinitionId
import com.inout.apiserver.base.enums.FsrsCardRating
import com.inout.apiserver.base.enums.FsrsCardState
import com.inout.apiserver.domain.study.StudyCreateObject
import com.inout.apiserver.error.InternalServerErrorException
import com.inout.apiserver.infrastructure.db.TimestampedEntity
import com.inout.fsrs.FSRS
import com.inout.fsrs.model.Card
import com.inout.fsrs.model.FSRSParameters
import com.inout.fsrs.model.enums.Grade
import jakarta.persistence.CascadeType
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import org.hibernate.annotations.DynamicInsert
import org.hibernate.annotations.DynamicUpdate
import java.time.Instant

@Entity
@Table(
    name = "studies",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["user_id", "word_definition_id"]),
    ],
)
@DynamicUpdate
@DynamicInsert
data class Study(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: StudyId? = null,
    val userId: UserId,
    // should be unmodifiable
    val wordDefinitionId: WordDefinitionId,
    @Enumerated(EnumType.STRING)
    val state: FsrsCardState,
    val due: Instant,
    val stability: Double,
    val difficulty: Double,
    val elapsedDays: Int,
    val scheduledDays: Int,
    val reps: Int,
    val lapses: Int,
    val lastReview: Instant?,
    @OneToMany(cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
    @JoinColumn(name = "study_id")
    val reviewLogs: List<StudyReviewLog> = emptyList(),
) : TimestampedEntity() {
    companion object {
        fun fromCreateObject(createObject: StudyCreateObject): Study {
            val fsrsCard = Card.createEmptyCard()
            return Study(
                userId = createObject.userId,
                wordDefinitionId = createObject.wordDefinitionId,
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

    fun rate(rating: FsrsCardRating): Study {
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
            reviewLogs = reviewLogs + StudyReviewLog.fromFsrsReviewLog(newFsrsReviewLog),
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Study

        if (id != other.id) return false
        if (userId != other.userId) return false
        if (wordDefinitionId != other.wordDefinitionId) return false
        if (stability != other.stability) return false
        if (difficulty != other.difficulty) return false
        if (elapsedDays != other.elapsedDays) return false
        if (scheduledDays != other.scheduledDays) return false
        if (reps != other.reps) return false
        if (lapses != other.lapses) return false
        if (state != other.state) return false
        if (due != other.due) return false
        if (lastReview != other.lastReview) return false
        // Use Iterables.elementsEqual to compare two lists of reviewLogs because reviewLogs may be a proxy object
        if (!Iterables.elementsEqual(reviewLogs, other.reviewLogs)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + userId.hashCode()
        result = 31 * result + wordDefinitionId.hashCode()
        result = 31 * result + stability.hashCode()
        result = 31 * result + difficulty.hashCode()
        result = 31 * result + elapsedDays
        result = 31 * result + scheduledDays
        result = 31 * result + reps
        result = 31 * result + lapses
        result = 31 * result + state.hashCode()
        result = 31 * result + due.hashCode()
        result = 31 * result + (lastReview?.hashCode() ?: 0)
        result = 31 * result + reviewLogs.toList().hashCode()

        return result
    }
}

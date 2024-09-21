package com.inout.apiserver.infrastructure.db.study

import com.inout.apiserver.base.enums.FsrsCardState
import com.inout.apiserver.domain.study.Study
import com.inout.apiserver.domain.study.StudyCreateObject
import com.inout.apiserver.error.InOutRequireNotNullException
import com.inout.apiserver.infrastructure.db.BaseEntity
import com.inout.fsrs.model.Card
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.Instant

@Entity
@Table(
    name = "studies",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["user_id", "word_definition_id"]),
    ],
)
data class StudyEntity(
    val userId: Long,
    val wordDefinitionId: Long,
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
) : BaseEntity() {
    fun toDomain(): Study {
        return Study(
            id = id ?: throw InOutRequireNotNullException("Study id is null", "IORNN_STUDY_1"),
            userId = userId,
            wordDefinitionId = wordDefinitionId,
            createdAt = createdAt,
            updatedAt = updatedAt,
            state = state,
            due = due,
            stability = stability,
            difficulty = difficulty,
            elapsedDays = elapsedDays,
            scheduledDays = scheduledDays,
            reps = reps,
            lapses = lapses,
            lastReview = lastReview,
        )
    }

    companion object {
        fun fromCreateObject(studyCreateObject: StudyCreateObject): StudyEntity {
            val fsrsCard = Card.createEmptyCard()

            return StudyEntity(
                userId = studyCreateObject.userId,
                wordDefinitionId = studyCreateObject.wordDefinitionId,
                state = FsrsCardState.of(fsrsCard.state),
                due = fsrsCard.due,
                stability = fsrsCard.stability,
                difficulty = fsrsCard.difficulty,
                elapsedDays = fsrsCard.elapsedDays,
                scheduledDays = fsrsCard.scheduledDays,
                reps = fsrsCard.reps,
                lapses = fsrsCard.lapses,
                lastReview = fsrsCard.lastReview,
            )
        }
    }
}

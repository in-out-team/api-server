package com.inout.apiserver.infrastructure.db.study

import com.inout.apiserver.base.enums.FsrsCardState
import com.inout.apiserver.domain.study.Study
import com.inout.apiserver.domain.study.StudyCreateObject
import com.inout.apiserver.error.InOutRequireNotNullException
import com.inout.apiserver.infrastructure.db.BaseEntity
import com.inout.fsrs.model.Card
import jakarta.persistence.CascadeType
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
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
    @OneToMany(cascade = [CascadeType.ALL])
    @JoinColumn(name = "study_id")
    val reviewLogs: List<StudyReviewLogEntity>,
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
            reviewLogs = reviewLogs.map { it.toDomain() },
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
                reviewLogs = emptyList(),
            )
        }

        fun fromDomain(study: Study): StudyEntity {
            return StudyEntity(
                userId = study.userId,
                wordDefinitionId = study.wordDefinitionId,
                state = study.state,
                due = study.due,
                stability = study.stability,
                difficulty = study.difficulty,
                elapsedDays = study.elapsedDays,
                scheduledDays = study.scheduledDays,
                reps = study.reps,
                lapses = study.lapses,
                lastReview = study.lastReview,
                reviewLogs = study.reviewLogs.map { StudyReviewLogEntity.fromDomain(it) },
            ).apply {
                id = if (study.id <= 0) null else study.id
                createdAt = study.createdAt
                updatedAt = study.updatedAt
            }
        }
    }
}

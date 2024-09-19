package com.inout.apiserver.infrastructure.db.study

import com.inout.apiserver.domain.study.Study
import com.inout.apiserver.domain.study.StudyCreateObject
import com.inout.apiserver.error.InOutRequireNotNullException
import com.inout.apiserver.infrastructure.db.BaseEntity
import jakarta.persistence.Entity
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint

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
) : BaseEntity() {
    fun toDomain(): Study {
        return Study(
            id = id ?: throw InOutRequireNotNullException("Study id is null", "IORNN_STUDY_1"),
            userId = userId,
            wordDefinitionId = wordDefinitionId,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
    }

    companion object {
        fun fromCreateObject(studyCreateObject: StudyCreateObject): StudyEntity {
            return StudyEntity(
                userId = studyCreateObject.userId,
                wordDefinitionId = studyCreateObject.wordDefinitionId,
            )
        }
    }
}

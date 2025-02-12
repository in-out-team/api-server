package com.inout.apiserver.infrastructure.db.study

import com.inout.apiserver.base.alias.DailyStudySetId
import com.inout.apiserver.base.alias.StudyId
import com.inout.apiserver.base.alias.UserId
import com.inout.apiserver.domain.study.DailyStudySetCreateObject
import com.inout.apiserver.infrastructure.db.TimestampedEntity
import io.hypersistence.utils.hibernate.type.array.ListArrayType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import org.hibernate.annotations.DynamicInsert
import org.hibernate.annotations.DynamicUpdate
import org.hibernate.annotations.Type
import java.time.LocalDate

@Entity
@Table(
    name = "daily_study_sets",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["user_id", "date"]),
    ],
)
@DynamicUpdate
@DynamicInsert
data class DailyStudySet(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: DailyStudySetId? = null,
    val userId: UserId,
    val date: LocalDate,
    @Column(columnDefinition = "bigint[]", nullable = false, updatable = true, name = "study_ids")
    @Type(ListArrayType::class)
    val studyIds: List<StudyId>,
) : TimestampedEntity() {
    companion object {
        fun fromCreateObject(createObject: DailyStudySetCreateObject): DailyStudySet =
            DailyStudySet(
                userId = createObject.userId,
                date = createObject.date,
                studyIds = emptyList(),
            )
    }
}

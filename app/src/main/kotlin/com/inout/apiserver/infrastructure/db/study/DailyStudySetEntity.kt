package com.inout.apiserver.infrastructure.db.study

import com.inout.apiserver.domain.study.DailyStudySet
import com.inout.apiserver.domain.study.DailyStudySetCreateObject
import com.inout.apiserver.error.InOutRequireNotNullException
import com.inout.apiserver.infrastructure.db.BaseEntity
import io.hypersistence.utils.hibernate.type.array.ListArrayType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import org.hibernate.annotations.Type
import java.time.LocalDate

@Entity
@Table(
    name = "daily_study_sets",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["user_id", "date"]),
    ],
)
data class DailyStudySetEntity(
    val userId: Long,
    val date: LocalDate,
    @Column(columnDefinition = "bigint[]", nullable = false, updatable = true, name = "study_ids")
    @Type(ListArrayType::class)
    val studyIds: List<Long>,
) : BaseEntity() {
    fun toDomain(): DailyStudySet {
        return DailyStudySet(
            id = id ?: throw InOutRequireNotNullException("DailyStudySet id is null", "IORNN_DAILY_STUDY_SET_1"),
            userId = userId,
            studyIds = studyIds,
            date = date,
        )
    }

    companion object {
        fun fromCreateObject(dailyStudySetCreateObject: DailyStudySetCreateObject): DailyStudySetEntity {
            return DailyStudySetEntity(
                userId = dailyStudySetCreateObject.userId,
                studyIds = emptyList(),
                date = dailyStudySetCreateObject.date,
            )
        }

        fun fromDomain(dailyStudySet: DailyStudySet): DailyStudySetEntity {
            return DailyStudySetEntity(
                userId = dailyStudySet.userId,
                studyIds = dailyStudySet.studyIds,
                date = dailyStudySet.date,
            ).apply {
                id = if (dailyStudySet.id <= 0) null else dailyStudySet.id
            }
        }
    }
}

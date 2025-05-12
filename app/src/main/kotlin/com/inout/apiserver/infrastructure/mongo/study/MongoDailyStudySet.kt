package com.inout.apiserver.infrastructure.mongo.study

import org.bson.types.ObjectId
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.index.CompoundIndexes
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant
import java.time.LocalDate

@Document(collection = "daily_study_sets")
@CompoundIndexes(
    CompoundIndex(
        name = "unique_user_id_date",
        def = "{'userId': 1, 'date': 1}",
        unique = true,
    ),
)
data class MongoDailyStudySet(
    @Id
    val id: ObjectId? = null,
    val userId: ObjectId,
    val date: LocalDate,
    val studyIds: List<ObjectId> = emptyList(),
    @CreatedDate
    val createdAt: Instant? = null,
    @LastModifiedDate
    val updatedAt: Instant? = null,
) {
    companion object {
        // FIXME: temporary
        fun fromCreateObject(
            userId: ObjectId,
            date: LocalDate,
        ): MongoDailyStudySet =
            MongoDailyStudySet(
                userId = userId,
                date = date,
                studyIds = emptyList(),
            )
    }
}

package com.inout.apiserver.infrastructure.mongo.word

import org.bson.types.ObjectId
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.index.CompoundIndexes
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document(collection = "conversations")
@CompoundIndexes(
    CompoundIndex(
        name = "idx_userId_wordDefinitionId",
        def = "{'userId': 1, 'wordDefinitionId': 1}",
    ),
)
data class Conversation(
    @Id
    val id: ObjectId? = null,
    val userId: ObjectId,
    val wordDefinitionId: ObjectId,
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
        ): Conversation =
            Conversation(
                userId = userId,
                wordDefinitionId = wordDefinitionId,
            )
    }
}

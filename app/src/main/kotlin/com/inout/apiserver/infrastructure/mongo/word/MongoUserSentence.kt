package com.inout.apiserver.infrastructure.mongo.word

import com.inout.apiserver.base.enums.SentenceType
import org.bson.types.ObjectId
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.index.CompoundIndexes
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document(collection = "user_sentences")
@CompoundIndexes(
    CompoundIndex(
        name = "idx_userId_wordDefinitionId",
        def = "{'userId': 1, 'wordDefinitionId': 1}",
    ),
    CompoundIndex(
        name = "idx_userId_sentenceId",
        def = "{'userId': 1, 'sentenceId': 1}",
    ),
)
data class MongoUserSentence(
    @Id
    val id: ObjectId? = null,
    val userId: ObjectId,
    val wordDefinitionId: ObjectId,
    val type: SentenceType,
    val sentenceId: ObjectId,
    @CreatedDate
    val createdAt: Instant? = null,
    @LastModifiedDate
    val updatedAt: Instant? = null,
) {
    companion object {
        fun fromCreateObject(
            userId: ObjectId,
            wordDefinitionId: ObjectId,
            type: SentenceType,
            sentenceId: ObjectId,
        ): MongoUserSentence =
            MongoUserSentence(
                userId = userId,
                wordDefinitionId = wordDefinitionId,
                type = type,
                sentenceId = sentenceId,
            )
    }
}

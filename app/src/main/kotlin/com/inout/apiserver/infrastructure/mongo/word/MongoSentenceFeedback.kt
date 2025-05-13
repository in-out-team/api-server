package com.inout.apiserver.infrastructure.mongo.word

import org.bson.types.ObjectId
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.index.CompoundIndexes
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document(collection = "sentence_feedbacks")
@CompoundIndexes(
    CompoundIndex(
        name = "idx_sentenceId",
        def = "{'sentenceId': 1}",
    ),
)
data class MongoSentenceFeedback(
    @Id
    val id: ObjectId? = null,
    val sentenceId: ObjectId,
    val submittedContent: String,
    val feedback: String,
    @CreatedDate
    val createdAt: Instant? = null,
    @LastModifiedDate
    val updatedAt: Instant? = null,
) {
    companion object {
        // FIXME: temporary
        fun fromCreateObject(
            sentenceId: ObjectId,
            submittedContent: String,
            feedback: String,
        ): MongoSentenceFeedback =
            MongoSentenceFeedback(
                sentenceId = sentenceId,
                submittedContent = submittedContent,
                feedback = feedback,
            )
    }
}

package com.inout.apiserver.infrastructure.mongo.word

import org.bson.types.ObjectId
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.index.CompoundIndexes
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

/**
 * stores user's feedback on writing sentence practice.
 */
@Document(collection = "user_sentence_feedbacks")
@CompoundIndexes(
    CompoundIndex(
        name = "idx_userSentenceId",
        def = "{'userSentenceId': 1}",
    ),
)
data class UserSentenceFeedback(
    @Id
    val id: ObjectId? = null,
    val userSentenceId: ObjectId,
    val sentenceFeedbackId: ObjectId,
    @CreatedDate
    val createdAt: Instant? = null,
    @LastModifiedDate
    val updatedAt: Instant? = null,
) {
    companion object {
        fun fromCreateObject(
            userSentenceId: ObjectId,
            sentenceFeedbackId: ObjectId,
        ): UserSentenceFeedback =
            UserSentenceFeedback(
                userSentenceId = userSentenceId,
                sentenceFeedbackId = sentenceFeedbackId,
            )
    }
}

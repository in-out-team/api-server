package com.inout.apiserver.infrastructure.db.word

import com.inout.apiserver.base.alias.SentenceFeedbackId
import com.inout.apiserver.base.alias.UserSentenceFeedbackId
import com.inout.apiserver.base.alias.UserSentenceId
import com.inout.apiserver.domain.word.UserSentenceFeedbackCreateObject
import com.inout.apiserver.infrastructure.db.TimestampedEntity
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table

/**
 * class which stores user's feedback on writing sentence practice.
 */
@Entity
@Table(
    name = "user_sentence_feedbacks",
    indexes = [
        Index(columnList = "user_sentence_id", name = "idx_user_sentence_feedbacks_user_sentence_id"),
    ],
)
data class UserSentenceFeedback(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: UserSentenceFeedbackId? = null,
    val userSentenceId: UserSentenceId,
    val sentenceFeedbackId: SentenceFeedbackId,
) : TimestampedEntity() {
    companion object {
        fun fromCreateObject(userSentenceFeedbackCreateObject: UserSentenceFeedbackCreateObject): UserSentenceFeedback =
            UserSentenceFeedback(
                userSentenceId = userSentenceFeedbackCreateObject.userSentenceId,
                sentenceFeedbackId = userSentenceFeedbackCreateObject.sentenceFeedbackId,
            )
    }
}

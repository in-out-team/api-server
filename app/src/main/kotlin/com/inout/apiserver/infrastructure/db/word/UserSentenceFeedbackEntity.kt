package com.inout.apiserver.infrastructure.db.word

import com.inout.apiserver.domain.word.UserSentenceFeedback
import com.inout.apiserver.domain.word.UserSentenceFeedbackCreateObject
import com.inout.apiserver.error.InOutRequireNotNullException
import com.inout.apiserver.infrastructure.db.BaseEntity
import jakarta.persistence.Entity
import jakarta.persistence.Index
import jakarta.persistence.Table

@Entity
@Table(
    name = "user_sentence_feedbacks",
    indexes = [
        Index(columnList = "user_sentence_id", name = "idx_user_sentence_feedbacks_user_sentence_id"),
    ],
)
data class UserSentenceFeedbackEntity(
    val userSentenceId: Long,
    val sentenceFeedbackId: Long,
) : BaseEntity() {
    companion object {
        fun fromCreateObject(userSentenceFeedbackCreateObject: UserSentenceFeedbackCreateObject): UserSentenceFeedbackEntity =
            UserSentenceFeedbackEntity(
                userSentenceId = userSentenceFeedbackCreateObject.userSentenceId,
                sentenceFeedbackId = userSentenceFeedbackCreateObject.sentenceFeedbackId,
            )
    }

    fun toDomain(): UserSentenceFeedback =
        UserSentenceFeedback(
            id =
                id ?: throw InOutRequireNotNullException(
                    "UserSentenceFeedback id is null",
                    "IORNN_USER_SENTENCE_FEEDBACK_1",
                ),
            userSentenceId = userSentenceId,
            sentenceFeedbackId = sentenceFeedbackId,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
}

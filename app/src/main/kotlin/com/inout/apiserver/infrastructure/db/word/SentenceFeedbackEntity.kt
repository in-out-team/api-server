package com.inout.apiserver.infrastructure.db.word

import com.inout.apiserver.domain.word.SentenceFeedback
import com.inout.apiserver.domain.word.SentenceFeedbackCreateObject
import com.inout.apiserver.error.InOutRequireNotNullException
import com.inout.apiserver.infrastructure.db.BaseEntity
import jakarta.persistence.Entity
import jakarta.persistence.Index
import jakarta.persistence.Table

@Entity
@Table(
    name = "sentence_feedbacks",
    indexes = [
        Index(columnList = "sentence_id", name = "idx_sentence_feedbacks_sentence_id"),
    ],
)
data class SentenceFeedbackEntity(
    val sentenceId: Long,
    val submittedContent: String,
    val feedback: String,
    // add provider? currently only support OpenAI, but may support other providers in the future
) : BaseEntity() {
    companion object {
        fun fromCreateObject(sentenceFeedbackCreateObject: SentenceFeedbackCreateObject): SentenceFeedbackEntity =
            SentenceFeedbackEntity(
                sentenceId = sentenceFeedbackCreateObject.sentenceId,
                submittedContent = sentenceFeedbackCreateObject.submittedContent,
                feedback = sentenceFeedbackCreateObject.feedback,
            )
    }

    fun toDomain(): SentenceFeedback =
        SentenceFeedback(
            id = id ?: throw InOutRequireNotNullException("SentenceFeedback id is null", "IORNN_SENTENCE_FEEDBACK_1"),
            sentenceId = sentenceId,
            submittedContent = submittedContent,
            feedback = feedback,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
}

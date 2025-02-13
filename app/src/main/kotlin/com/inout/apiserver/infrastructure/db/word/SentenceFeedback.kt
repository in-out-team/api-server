package com.inout.apiserver.infrastructure.db.word

import com.inout.apiserver.base.alias.SentenceFeedbackId
import com.inout.apiserver.base.alias.SentenceId
import com.inout.apiserver.domain.word.SentenceFeedbackCreateObject
import com.inout.apiserver.infrastructure.db.TimestampedEntity
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table

@Entity
@Table(
    name = "sentence_feedbacks",
    indexes = [
        Index(columnList = "sentence_id", name = "idx_sentence_feedbacks_sentence_id"),
    ],
)
data class SentenceFeedback(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: SentenceFeedbackId? = null,
    val sentenceId: SentenceId,
    val submittedContent: String,
    val feedback: String,
    // add provider? currently only support OpenAI, but may support other providers in the future
) : TimestampedEntity() {
    companion object {
        fun fromCreateObject(sentenceFeedbackCreateObject: SentenceFeedbackCreateObject) =
            SentenceFeedback(
                sentenceId = sentenceFeedbackCreateObject.sentenceId,
                submittedContent = sentenceFeedbackCreateObject.submittedContent,
                feedback = sentenceFeedbackCreateObject.feedback,
            )
    }
}

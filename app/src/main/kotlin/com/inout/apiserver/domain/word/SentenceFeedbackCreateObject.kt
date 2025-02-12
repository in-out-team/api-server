package com.inout.apiserver.domain.word

import com.inout.apiserver.base.alias.SentenceId

data class SentenceFeedbackCreateObject(
    val sentenceId: SentenceId,
    val submittedContent: String,
    val feedback: String,
) {
    init {
        require(sentenceId > 0) { "sentenceId must be greater than 0" }
        require(feedback.isNotBlank()) { "feedback must not be blank" }
    }
}

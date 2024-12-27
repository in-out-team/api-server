package com.inout.apiserver.domain.word

import java.time.Instant

data class SentenceFeedback(
    val id: Long,
    val sentenceId: Long,
    val submittedContent: String,
    val feedback: String,
    val createdAt: Instant? = null,
    val updatedAt: Instant? = null,
)

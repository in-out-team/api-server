package com.inout.apiserver.domain.word

import java.time.Instant

/**
 * class which stores user's feedback on writing sentence practice.
 */
data class UserSentenceFeedback(
    val id: Long,
    val userSentenceId: Long,
    val sentenceFeedbackId: Long,
    val createdAt: Instant? = null,
    val updatedAt: Instant? = null,
)

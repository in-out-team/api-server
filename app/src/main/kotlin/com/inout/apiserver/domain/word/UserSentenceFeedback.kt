package com.inout.apiserver.domain.word

import com.inout.apiserver.base.alias.UserSentenceId
import java.time.Instant

/**
 * class which stores user's feedback on writing sentence practice.
 */
data class UserSentenceFeedback(
    val id: Long,
    val userSentenceId: UserSentenceId,
    val sentenceFeedbackId: Long,
    val createdAt: Instant? = null,
    val updatedAt: Instant? = null,
)

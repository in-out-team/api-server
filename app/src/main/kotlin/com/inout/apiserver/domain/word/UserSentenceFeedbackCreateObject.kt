package com.inout.apiserver.domain.word

import com.inout.apiserver.base.alias.SentenceFeedbackId
import com.inout.apiserver.base.alias.UserSentenceId

data class UserSentenceFeedbackCreateObject(
    val userSentenceId: UserSentenceId,
    val sentenceFeedbackId: SentenceFeedbackId,
)

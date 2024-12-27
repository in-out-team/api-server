package com.inout.apiserver.domain.word

data class UserSentenceFeedbackCreateObject(
    val userId: Long,
    val sentenceId: Long,
    val sentenceFeedbackId: Long,
)

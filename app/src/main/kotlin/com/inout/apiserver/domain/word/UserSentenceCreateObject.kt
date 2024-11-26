package com.inout.apiserver.domain.word

data class UserSentenceCreateObject(
    val userId: Long,
    val wordDefinitionId: Long,
    val sentenceId: Long,
)

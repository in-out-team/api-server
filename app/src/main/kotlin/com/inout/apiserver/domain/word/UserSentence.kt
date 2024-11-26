package com.inout.apiserver.domain.word

data class UserSentence(
    val id: Long,
    val userId: Long,
    val wordDefinitionId: Long,
    val sentenceId: Long,
)

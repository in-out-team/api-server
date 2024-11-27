package com.inout.apiserver.domain.word

import java.time.Instant

data class UserSentence(
    val id: Long,
    val userId: Long,
    val wordDefinitionId: Long,
    val sentenceId: Long,
    val createdAt: Instant?,
    val updatedAt: Instant?,
)

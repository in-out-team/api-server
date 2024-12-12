package com.inout.apiserver.domain.word

import com.inout.apiserver.base.enums.SentenceType
import java.time.Instant

data class UserSentence(
    val id: Long,
    val userId: Long,
    val wordDefinitionId: Long,
    val type: SentenceType,
    val sentenceId: Long,
    val createdAt: Instant?,
    val updatedAt: Instant?,
)

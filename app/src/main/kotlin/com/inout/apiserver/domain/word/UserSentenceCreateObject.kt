package com.inout.apiserver.domain.word

import com.inout.apiserver.base.enums.SentenceType

data class UserSentenceCreateObject(
    val userId: Long,
    val wordDefinitionId: Long,
    val type: SentenceType,
    val sentenceId: Long,
)

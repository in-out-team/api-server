package com.inout.apiserver.domain.word

import com.inout.apiserver.base.alias.SentenceId
import com.inout.apiserver.base.alias.UserId
import com.inout.apiserver.base.alias.WordDefinitionId
import com.inout.apiserver.base.enums.SentenceType

data class UserSentenceCreateObject(
    val userId: UserId,
    val wordDefinitionId: WordDefinitionId,
    val type: SentenceType,
    val sentenceId: SentenceId,
)

package com.inout.apiserver.domain.word

import com.inout.apiserver.base.alias.UserId
import com.inout.apiserver.base.alias.WordDefinitionId

data class ConversationCreateObject(
    val userId: UserId,
    val wordDefinitionId: WordDefinitionId,
)

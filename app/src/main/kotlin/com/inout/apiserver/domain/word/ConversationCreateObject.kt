package com.inout.apiserver.domain.word

import com.inout.apiserver.base.alias.UserId
import com.inout.apiserver.base.alias.WordDefinitionId
import com.inout.apiserver.infrastructure.db.word.AiAudio

data class ConversationCreateObject(
    val userId: UserId,
    val wordDefinitionId: WordDefinitionId,
    val systemMessage: String,
    val systemAudio: AiAudio,
)

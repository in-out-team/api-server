package com.inout.apiserver.domain.word

import java.time.Instant

data class Conversation(
    val id: Long,
    val userId: Long,
    val wordDefinitionId: Long,
    val messages: List<ConversationMessage>,
    val createdAt: Instant? = null,
    val updatedAt: Instant? = null,
)

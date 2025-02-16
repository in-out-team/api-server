package com.inout.apiserver.interfaces.web.v1.response

import com.inout.apiserver.infrastructure.db.word.Conversation
import java.time.Instant

data class ConversationResponse(
    val id: Long,
    val userId: Long,
    val wordDefinitionId: Long,
    val messages: List<ConversationMessageResponse>,
    val createdAt: Instant? = null,
) {
    companion object {
        fun of(conversation: Conversation): ConversationResponse =
            ConversationResponse(
                id = conversation.id!!,
                userId = conversation.userId,
                wordDefinitionId = conversation.wordDefinitionId,
                messages = conversation.messages.map { ConversationMessageResponse.of(it) },
                createdAt = conversation.createdAt,
            )
    }
}

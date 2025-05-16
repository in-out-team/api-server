package com.inout.apiserver.interfaces.web.v1.response

import com.inout.apiserver.domain.word.ConversationWithMessages
import org.bson.types.ObjectId
import java.time.Instant

data class ConversationResponse(
    val id: ObjectId,
    val userId: ObjectId,
    val wordDefinitionId: ObjectId,
    val messages: List<ConversationMessageResponse>,
    val createdAt: Instant? = null,
) {
    companion object {
        fun of(conversation: ConversationWithMessages): ConversationResponse =
            ConversationResponse(
                id = conversation.id!!,
                userId = conversation.userId,
                wordDefinitionId = conversation.wordDefinitionId,
                messages = conversation.messages.map { ConversationMessageResponse.of(it) },
                createdAt = conversation.createdAt,
            )
    }
}

package com.inout.apiserver.interfaces.web.v1.response

import com.inout.apiserver.domain.word.ConversationWithMessages
import org.bson.types.ObjectId
import java.time.Instant

data class MongoConversationResponse(
    val id: ObjectId,
    val userId: ObjectId,
    val wordDefinitionId: ObjectId,
    val messages: List<MongoConversationMessageResponse>,
    val createdAt: Instant? = null,
) {
    companion object {
        fun of(conversation: ConversationWithMessages): MongoConversationResponse =
            MongoConversationResponse(
                id = conversation.id!!,
                userId = conversation.userId,
                wordDefinitionId = conversation.wordDefinitionId,
                messages = conversation.messages.map { MongoConversationMessageResponse.of(it) },
                createdAt = conversation.createdAt,
            )
    }
}

package com.inout.apiserver.interfaces.web.v1.response

import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.inout.apiserver.base.enums.SenderType
import com.inout.apiserver.base.serializer.PreSignedUrlSerializer
import com.inout.apiserver.domain.word.ConversationWithMessages
import org.bson.types.ObjectId
import java.time.Instant

data class ConversationMessageResponse(
    val id: ObjectId,
    val senderType: SenderType,
    val content: String,
    @JsonSerialize(using = PreSignedUrlSerializer::class)
    val audioUrl: String?,
    val createdAt: Instant? = null,
) {
    companion object {
        fun of(conversationMessage: ConversationWithMessages.ConversationMessage): ConversationMessageResponse =
            ConversationMessageResponse(
                id = conversationMessage.id!!,
                senderType = conversationMessage.sender,
                content = conversationMessage.content,
                audioUrl = conversationMessage.audio?.directory,
                createdAt = conversationMessage.createdAt,
            )
    }
}

package com.inout.apiserver.interfaces.web.v1.response

import com.inout.apiserver.base.enums.SenderType
import com.inout.apiserver.domain.word.ConversationMessage
import java.time.Instant

data class ConversationMessageResponse(
    val id: Long,
    val sender: SenderType,
    val content: String,
    val audioUrl: String?,
    val createdAt: Instant? = null,
) {
    companion object {
        fun of(conversationMessage: ConversationMessage): ConversationMessageResponse =
            ConversationMessageResponse(
                id = conversationMessage.id,
                sender = conversationMessage.sender,
                content = conversationMessage.content,
                audioUrl = conversationMessage.audio?.url,
                createdAt = conversationMessage.createdAt,
            )
    }
}

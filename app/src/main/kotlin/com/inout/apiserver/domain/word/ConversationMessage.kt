package com.inout.apiserver.domain.word

import com.inout.apiserver.base.enums.SenderType
import java.time.Instant

data class ConversationMessage(
    val id: Long,
    val conversationId: Long,
    val sender: SenderType,
    val content: String,
    val audio: AiAudio?,
    val createdAt: Instant? = null,
    val updatedAt: Instant? = null,
)

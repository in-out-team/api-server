package com.inout.apiserver.domain.word

import com.inout.apiserver.base.enums.AiVoiceType
import com.inout.apiserver.base.enums.LanguageType
import com.inout.apiserver.base.enums.SenderType
import org.bson.types.ObjectId
import java.time.Instant

data class ConversationWithMessages(
    val id: ObjectId? = null,
    val userId: ObjectId,
    val wordDefinitionId: ObjectId,
    val messages: List<ConversationMessage>,
    val createdAt: Instant,
) {
    data class ConversationMessage(
        val id: ObjectId? = null,
        val sender: SenderType,
        val content: String,
        val audio: AiAudio? = null,
    )

    data class AiAudio(
        val id: ObjectId? = null,
        val language: LanguageType,
        val voiceType: AiVoiceType,
        val content: String,
        val directory: String,
    )
}

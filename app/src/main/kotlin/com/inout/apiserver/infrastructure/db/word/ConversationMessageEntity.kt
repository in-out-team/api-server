package com.inout.apiserver.infrastructure.db.word

import com.inout.apiserver.base.enums.SenderType
import com.inout.apiserver.domain.word.ConversationMessage
import com.inout.apiserver.error.InOutRequireNotNullException
import com.inout.apiserver.infrastructure.db.BaseEntity
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "conversation_messages")
data class ConversationMessageEntity(
    @Enumerated(EnumType.STRING)
    val sender: SenderType,
    val content: String,
    @ManyToOne(optional = true)
    val audio: AiAudioEntity? = null,
) : BaseEntity() {
    fun toDomain(): ConversationMessage =
        ConversationMessage(
            id = id ?: throw InOutRequireNotNullException("ConversationMessage id is null", "IORNN_CONVERSATION_MESSAGE_1"),
            sender = sender,
            content = content,
            audio = audio?.toDomain(),
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
}

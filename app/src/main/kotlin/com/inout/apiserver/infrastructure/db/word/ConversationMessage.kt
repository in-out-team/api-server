package com.inout.apiserver.infrastructure.db.word

import com.inout.apiserver.base.alias.ConversationMessageId
import com.inout.apiserver.base.enums.SenderType
import com.inout.apiserver.infrastructure.db.TimestampedEntity
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "conversation_messages")
data class ConversationMessage(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: ConversationMessageId? = null,
    @Enumerated(EnumType.STRING)
    val sender: SenderType,
    val content: String,
    @ManyToOne(optional = true)
    val audio: AiAudio? = null,
) : TimestampedEntity()

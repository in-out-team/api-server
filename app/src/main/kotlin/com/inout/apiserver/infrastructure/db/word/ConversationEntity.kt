package com.inout.apiserver.infrastructure.db.word

import com.inout.apiserver.domain.word.Conversation
import com.inout.apiserver.error.InOutRequireNotNullException
import com.inout.apiserver.infrastructure.db.BaseEntity
import jakarta.persistence.CascadeType
import jakarta.persistence.Entity
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToMany
import jakarta.persistence.Table

@Entity
@Table(name = "conversations")
data class ConversationEntity(
    val userId: Long,
    val wordDefinitionId: Long,
    @OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true)
    @JoinColumn(name = "conversation_id")
    val messages: MutableList<ConversationMessageEntity> = mutableListOf(),
) : BaseEntity() {
    fun toDomain(): Conversation =
        Conversation(
            id = id ?: throw InOutRequireNotNullException("Conversation id is null", "IORNN_CONVERSATION_1"),
            userId = userId,
            wordDefinitionId = wordDefinitionId,
            messages = messages.map { it.toDomain() },
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
}

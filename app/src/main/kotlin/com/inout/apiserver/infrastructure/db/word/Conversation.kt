package com.inout.apiserver.infrastructure.db.word

import com.google.common.collect.Iterables
import com.inout.apiserver.base.alias.ConversationId
import com.inout.apiserver.base.alias.UserId
import com.inout.apiserver.base.alias.WordDefinitionId
import com.inout.apiserver.base.enums.SenderType
import com.inout.apiserver.domain.word.ConversationCreateObject
import com.inout.apiserver.infrastructure.db.TimestampedEntity
import jakarta.persistence.CascadeType
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToMany
import jakarta.persistence.OrderBy
import jakarta.persistence.Table
import org.hibernate.annotations.DynamicInsert
import org.hibernate.annotations.DynamicUpdate

@Entity
@Table(
    name = "conversations",
    indexes = [
        Index(
            columnList = "user_id, word_definition_id",
            name = "idx_conversations_user_id_word_definition_id",
        ),
    ],
)
@DynamicInsert
@DynamicUpdate
data class Conversation(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: ConversationId? = null,
    val userId: UserId,
    val wordDefinitionId: WordDefinitionId,
    @OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "conversation_id")
    @OrderBy("createdAt ASC")
    val messages: MutableList<ConversationMessage> = mutableListOf(),
) : TimestampedEntity() {
    companion object {
        fun fromCreateObject(createObject: ConversationCreateObject): Conversation =
            Conversation(
                userId = createObject.userId,
                wordDefinitionId = createObject.wordDefinitionId,
                messages =
                    mutableListOf(
                        ConversationMessage(
                            sender = SenderType.SYSTEM,
                            content = createObject.systemAudio.content,
                            audio = createObject.systemAudio,
                        ),
                    ),
            )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Conversation

        if (id != other.id) return false
        if (userId != other.userId) return false
        if (wordDefinitionId != other.wordDefinitionId) return false
        if (!Iterables.elementsEqual(messages, other.messages)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + userId.hashCode()
        result = 31 * result + wordDefinitionId.hashCode()
        result = 31 * result + messages.toList().hashCode()

        return result
    }

    fun addUserMessage(messageContent: String): Conversation {
        messages.add(
            ConversationMessage(
                sender = SenderType.USER,
                content = messageContent,
            ),
        )
        return this
    }

    fun addSystemMessage(
        messageContent: String,
        audio: AiAudio,
    ): Conversation {
        messages.add(
            ConversationMessage(
                sender = SenderType.SYSTEM,
                content = messageContent,
                audio = audio,
            ),
        )
        return this
    }
}

package com.inout.apiserver.infrastructure.db.word

import com.inout.apiserver.base.alias.ConversationId
import com.inout.apiserver.base.alias.UserId
import com.inout.apiserver.base.alias.WordDefinitionId
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
    val messages: MutableList<ConversationMessage> = mutableListOf(),
) : TimestampedEntity()

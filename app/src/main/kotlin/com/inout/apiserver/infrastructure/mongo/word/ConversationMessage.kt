package com.inout.apiserver.infrastructure.mongo.word

import com.inout.apiserver.base.enums.SenderType
import org.bson.types.ObjectId
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.index.CompoundIndexes
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document(collection = "conversation_messages")
@CompoundIndexes(
    CompoundIndex(
        name = "idx_conversationId",
        def = "{'conversationId': 1}",
    ),
)
data class ConversationMessage(
    @Id
    val id: ObjectId? = null,
    val conversationId: ObjectId,
    val sender: SenderType,
    val content: String,
    // TODO: need to check how audio is stored in MongoDB
    val audio: AiAudio? = null,
    @CreatedDate
    val createdAt: Instant? = null,
    @LastModifiedDate
    val updatedAt: Instant? = null,
)

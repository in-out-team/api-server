package com.inout.apiserver.infrastructure.mongo.word

import com.inout.apiserver.domain.word.ConversationWithMessages
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

interface MongoConversationRepositoryInternal : MongoRepository<MongoConversation, ObjectId> {
    fun findAllByUserIdAndWordDefinitionId(
        userId: ObjectId,
        wordDefinitionId: ObjectId,
    ): List<MongoConversation>
}

interface MongoConversationMessageRepositoryInternal : MongoRepository<MongoConversationMessage, ObjectId> {
    fun findAllByConversationId(conversationId: ObjectId): List<MongoConversationMessage>
}

@Repository
class MongoConversationRepository(
    private val conversationRepository: MongoConversationRepositoryInternal,
    private val conversationMessageRepository: MongoConversationMessageRepositoryInternal,
) {
    fun findAllByUserIdAndWordDefinitionId(
        userId: ObjectId,
        wordDefinitionId: ObjectId,
    ): List<ConversationWithMessages> {
        TODO()
    }

    fun findById(id: ObjectId): ConversationWithMessages? {
        TODO()
    }

    fun saveConversation(conversation: MongoConversation): MongoConversation = conversationRepository.save(conversation)
}

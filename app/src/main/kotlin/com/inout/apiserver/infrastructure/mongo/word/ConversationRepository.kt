package com.inout.apiserver.infrastructure.mongo.word

import com.inout.apiserver.domain.word.ConversationWithMessages
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

interface ConversationRepositoryInternal : MongoRepository<Conversation, ObjectId> {
    fun findAllByUserIdAndWordDefinitionId(
        userId: ObjectId,
        wordDefinitionId: ObjectId,
    ): List<Conversation>
}

interface ConversationMessageRepositoryInternal : MongoRepository<ConversationMessage, ObjectId> {
    fun findAllByConversationId(conversationId: ObjectId): List<ConversationMessage>

    fun findAllByConversationIdIn(conversationIds: List<ObjectId>): List<ConversationMessage>
}

@Repository
class ConversationRepository(
    private val conversationRepository: ConversationRepositoryInternal,
    private val conversationMessageRepository: ConversationMessageRepositoryInternal,
    private val aiAudioRepository: AiAudioRepository,
) {
    fun findAllByUserIdAndWordDefinitionId(
        userId: ObjectId,
        wordDefinitionId: ObjectId,
    ): List<ConversationWithMessages> {
        val conversations =
            conversationRepository.findAllByUserIdAndWordDefinitionId(
                userId = userId,
                wordDefinitionId = wordDefinitionId,
            )
        val conversationIds = conversations.map { it.id!! }

        val messagesByConversationId =
            conversationMessageRepository
                .findAllByConversationIdIn(conversationIds)
                .groupBy { it.conversationId }

        val aiAudios =
            aiAudioRepository.findAllById(
                messagesByConversationId.values.flatten().mapNotNull { it.audio?.id },
            )

        return conversations.map { conversation ->
            val messages =
                messagesByConversationId[conversation.id]?.sortedBy { it.createdAt }
                    ?: emptyList()
            val conversationMessages =
                messages.map { message ->
                    val audio =
                        message.audio?.let { aiAudio ->
                            aiAudios.find { it.id == aiAudio.id }
                        }
                    ConversationWithMessages.ConversationMessage(
                        id = message.id,
                        sender = message.sender,
                        content = message.content,
                        audio =
                            audio?.let {
                                ConversationWithMessages.AiAudio(
                                    id = it.id,
                                    language = it.language,
                                    voiceType = it.voiceType,
                                    content = it.content,
                                    directory = it.directory,
                                )
                            },
                    )
                }
            ConversationWithMessages(
                id = conversation.id,
                userId = conversation.userId,
                wordDefinitionId = conversation.wordDefinitionId,
                messages = conversationMessages,
                createdAt = conversation.createdAt!!,
            )
        }
    }

    fun findById(id: ObjectId): ConversationWithMessages? {
        val conversation = conversationRepository.findById(id).orElse(null) ?: return null
        val messages = conversationMessageRepository.findAllByConversationId(id)
        val aiAudios = aiAudioRepository.findAllById(messages.mapNotNull { it.audio?.id })

        val conversationMessages =
            messages.map { message ->
                val audio =
                    message.audio?.let { aiAudio ->
                        aiAudios.find { it.id == aiAudio.id }
                    }
                ConversationWithMessages.ConversationMessage(
                    id = message.id,
                    sender = message.sender,
                    content = message.content,
                    audio =
                        audio?.let {
                            ConversationWithMessages.AiAudio(
                                id = it.id,
                                language = it.language,
                                voiceType = it.voiceType,
                                content = it.content,
                                directory = it.directory,
                            )
                        },
                )
            }

        return ConversationWithMessages(
            id = conversation.id,
            userId = conversation.userId,
            wordDefinitionId = conversation.wordDefinitionId,
            messages = conversationMessages,
            createdAt = conversation.createdAt!!,
        )
    }

    fun saveConversation(conversation: Conversation): Conversation = conversationRepository.save(conversation)

    fun saveConversationMessage(message: ConversationMessage): ConversationMessage = conversationMessageRepository.save(message)
}

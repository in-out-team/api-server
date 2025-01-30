package com.inout.apiserver.infrastructure.db.word

import com.inout.apiserver.domain.word.Conversation
import org.springframework.stereotype.Repository

@Repository
class ConversationRepository(
    private val conversationJpaRepository: ConversationJpaRepository,
) {
    fun save(conversation: ConversationEntity) = conversationJpaRepository.save(conversation).toDomain()

    fun findById(id: Long): Conversation? = conversationJpaRepository.findById(id).map { it.toDomain() }.orElse(null)

    // FIXME: sort by createdAt or id?
    fun findAllByUserIdAndWordDefinitionId(
        userId: Long,
        wordDefinitionId: Long,
    ): List<Conversation> = conversationJpaRepository.findAllByUserIdAndWordDefinitionId(userId, wordDefinitionId).map { it.toDomain() }
}

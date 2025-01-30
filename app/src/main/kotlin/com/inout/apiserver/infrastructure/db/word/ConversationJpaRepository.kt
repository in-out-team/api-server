package com.inout.apiserver.infrastructure.db.word

import org.springframework.data.jpa.repository.JpaRepository

interface ConversationJpaRepository : JpaRepository<ConversationEntity, Long> {
    fun findAllByUserIdAndWordDefinitionId(
        userId: Long,
        wordDefinitionId: Long,
    ): List<ConversationEntity>
}

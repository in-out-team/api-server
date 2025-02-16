package com.inout.apiserver.infrastructure.db.word

import com.inout.apiserver.base.alias.ConversationId
import com.inout.apiserver.base.alias.UserId
import com.inout.apiserver.base.alias.WordDefinitionId
import org.springframework.data.jpa.repository.JpaRepository

interface ConversationRepository : JpaRepository<Conversation, ConversationId> {
    fun findAllByUserIdAndWordDefinitionId(
        userId: UserId,
        wordDefinitionId: WordDefinitionId,
    ): List<Conversation>
}

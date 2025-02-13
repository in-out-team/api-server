package com.inout.apiserver.infrastructure.db.word

import com.inout.apiserver.base.alias.SentenceId
import com.inout.apiserver.base.alias.UserId
import com.inout.apiserver.base.alias.UserSentenceId
import com.inout.apiserver.base.alias.WordDefinitionId
import com.inout.apiserver.base.enums.SentenceType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserSentenceRepository : JpaRepository<UserSentence, UserSentenceId> {
    fun findByUserIdAndSentenceId(
        userId: UserId,
        sentenceId: SentenceId,
    ): UserSentence?

    fun findAllByUserIdAndWordDefinitionIdAndType(
        userId: UserId,
        wordDefinitionId: WordDefinitionId,
        type: SentenceType,
    ): List<UserSentence>
}

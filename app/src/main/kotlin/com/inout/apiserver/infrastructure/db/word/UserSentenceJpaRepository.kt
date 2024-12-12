package com.inout.apiserver.infrastructure.db.word

import com.inout.apiserver.base.enums.SentenceType
import org.springframework.data.jpa.repository.JpaRepository

interface UserSentenceJpaRepository : JpaRepository<UserSentenceEntity, Long> {
    fun findByUserIdAndWordDefinitionIdAndSentenceId(
        userId: Long,
        wordDefinitionId: Long,
        sentenceId: Long,
    ): UserSentenceEntity?

    fun findByUserIdAndSentenceId(
        userId: Long,
        sentenceId: Long,
    ): UserSentenceEntity?

    fun findAllByUserIdAndWordDefinitionIdAndType(
        userId: Long,
        wordDefinitionId: Long,
        type: SentenceType,
    ): List<UserSentenceEntity>
}

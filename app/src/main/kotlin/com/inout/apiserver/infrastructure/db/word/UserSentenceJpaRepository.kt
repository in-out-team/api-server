package com.inout.apiserver.infrastructure.db.word

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

    fun findAllByUserIdAndWordDefinitionId(
        userId: Long,
        wordDefinitionId: Long,
    ): List<UserSentenceEntity>
}

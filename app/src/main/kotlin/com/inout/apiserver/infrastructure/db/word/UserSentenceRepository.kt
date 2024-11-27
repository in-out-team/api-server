package com.inout.apiserver.infrastructure.db.word

import com.inout.apiserver.domain.word.UserSentence
import org.springframework.stereotype.Repository

@Repository
class UserSentenceRepository(
    private val userSentenceJpaRepository: UserSentenceJpaRepository,
) {
    fun save(userSentence: UserSentenceEntity): UserSentence = userSentenceJpaRepository.save(userSentence).toDomain()

    fun findByUserIdAndWordDefinitionIdAndSentenceId(
        userId: Long,
        wordDefinitionId: Long,
        sentenceId: Long,
    ): UserSentence? =
        userSentenceJpaRepository
            .findByUserIdAndWordDefinitionIdAndSentenceId(
                userId,
                wordDefinitionId,
                sentenceId,
            )?.toDomain()

    fun findAllByUserIdAndWordDefinitionId(
        userId: Long,
        wordDefinitionId: Long,
    ): List<UserSentence> =
        userSentenceJpaRepository
            .findAllByUserIdAndWordDefinitionId(
                userId,
                wordDefinitionId,
            ).map { it.toDomain() }

    fun findByUserIdAndSentenceId(
        userId: Long,
        sentenceId: Long,
    ): UserSentence? =
        userSentenceJpaRepository
            .findByUserIdAndSentenceId(
                userId,
                sentenceId,
            )?.toDomain()
}

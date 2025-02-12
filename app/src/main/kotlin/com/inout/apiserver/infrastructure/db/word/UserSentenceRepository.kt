package com.inout.apiserver.infrastructure.db.word

import com.inout.apiserver.base.alias.SentenceId
import com.inout.apiserver.base.alias.UserId
import com.inout.apiserver.base.alias.WordDefinitionId
import com.inout.apiserver.base.enums.SentenceType
import com.inout.apiserver.domain.word.UserSentence
import org.springframework.stereotype.Repository

@Repository
class UserSentenceRepository(
    private val userSentenceJpaRepository: UserSentenceJpaRepository,
) {
    fun save(userSentence: UserSentenceEntity): UserSentence = userSentenceJpaRepository.save(userSentence).toDomain()

    fun delete(userSentence: UserSentenceEntity) = userSentenceJpaRepository.delete(userSentence)

    fun findAllByUserIdAndWordDefinitionIdAndType(
        userId: UserId,
        wordDefinitionId: WordDefinitionId,
        type: SentenceType,
    ): List<UserSentence> =
        userSentenceJpaRepository
            .findAllByUserIdAndWordDefinitionIdAndType(
                userId,
                wordDefinitionId,
                type,
            ).map { it.toDomain() }

    fun findByUserIdAndSentenceId(
        userId: UserId,
        sentenceId: SentenceId,
    ): UserSentence? =
        userSentenceJpaRepository
            .findByUserIdAndSentenceId(
                userId,
                sentenceId,
            )?.toDomain()
}

package com.inout.apiserver.infrastructure.db.word

import com.inout.apiserver.domain.word.UserSentenceFeedback
import org.springframework.stereotype.Repository

@Repository
class UserSentenceFeedbackRepository(
    private val userSentenceFeedbackJpaRepository: UserSentenceFeedbackJpaRepository,
) {
    fun save(userSentenceFeedback: UserSentenceFeedbackEntity): UserSentenceFeedback =
        userSentenceFeedbackJpaRepository.save(userSentenceFeedback).toDomain()

    fun findAllByUserSentenceId(userSentenceId: Long): List<UserSentenceFeedback> =
        userSentenceFeedbackJpaRepository
            .findAllByUserSentenceId(userSentenceId)
            .map { it.toDomain() }
}

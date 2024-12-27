package com.inout.apiserver.infrastructure.db.word

import org.springframework.data.jpa.repository.JpaRepository

interface UserSentenceFeedbackJpaRepository : JpaRepository<UserSentenceFeedbackEntity, Long> {
    fun findAllByUserSentenceId(userSentenceId: Long): List<UserSentenceFeedbackEntity>
}

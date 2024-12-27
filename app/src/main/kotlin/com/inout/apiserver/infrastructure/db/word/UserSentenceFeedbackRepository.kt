package com.inout.apiserver.infrastructure.db.word

import org.springframework.stereotype.Repository

@Repository
class UserSentenceFeedbackRepository(
    private val userSentenceFeedbackJpaRepository: UserSentenceFeedbackJpaRepository,
)

package com.inout.apiserver.infrastructure.db.word

import org.springframework.stereotype.Repository

@Repository
class SentenceFeedbackRepository(
    private val sentenceFeedbackJpaRepository: SentenceFeedbackJpaRepository,
)

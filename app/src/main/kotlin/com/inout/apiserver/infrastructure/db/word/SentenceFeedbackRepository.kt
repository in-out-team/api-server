package com.inout.apiserver.infrastructure.db.word

import org.springframework.stereotype.Repository

@Repository
class SentenceFeedbackRepository(
    private val sentenceFeedbackJpaRepository: SentenceFeedbackJpaRepository,
) {
    fun save(sentenceFeedback: SentenceFeedbackEntity) = sentenceFeedbackJpaRepository.save(sentenceFeedback).toDomain()

    fun findBySentenceIdAndSubmittedContent(
        sentenceId: Long,
        submittedContent: String,
    ) = sentenceFeedbackJpaRepository
        .findBySentenceIdAndSubmittedContent(
            sentenceId,
            submittedContent,
        )?.toDomain()
}

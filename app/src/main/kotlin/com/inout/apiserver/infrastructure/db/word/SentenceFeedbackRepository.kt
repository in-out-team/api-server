package com.inout.apiserver.infrastructure.db.word

import com.inout.apiserver.base.alias.SentenceFeedbackId
import com.inout.apiserver.base.alias.SentenceId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface SentenceFeedbackRepository : JpaRepository<SentenceFeedback, SentenceFeedbackId> {
    fun findBySentenceIdAndSubmittedContent(
        sentenceId: SentenceId,
        submittedContent: String,
    ): SentenceFeedback?

    fun findAllByIdIn(ids: List<SentenceFeedbackId>): List<SentenceFeedback>
}

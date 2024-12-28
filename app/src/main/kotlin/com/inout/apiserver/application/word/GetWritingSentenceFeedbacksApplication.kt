package com.inout.apiserver.application.word

import com.inout.apiserver.base.enums.SentenceType
import com.inout.apiserver.domain.user.User
import com.inout.apiserver.domain.word.SentenceFeedback
import com.inout.apiserver.domain.word.WordService
import com.inout.apiserver.error.NotFoundException
import org.springframework.stereotype.Component

@Component
class GetWritingSentenceFeedbacksApplication(
    private val wordService: WordService,
) {
    data class Request(
        val sentenceId: Long,
        val user: User,
    )

    data class Response(
        val feedbacks: List<SentenceFeedback>,
    )

    fun run(request: Request): Response {
        wordService.getSentenceByIdAndType(request.sentenceId, SentenceType.WRITING)
            ?: throw NotFoundException(
                message = "Sentence not found for id: ${request.sentenceId}",
                code = "SENTENCE_3",
            )
        val userSentence =
            wordService.getUserSentenceBy(request.user.id, request.sentenceId)
                ?: return Response(feedbacks = emptyList())
        val sentenceFeedbacks =
            wordService.getUserSentenceFeedbacks(userSentence.id).let { userSentenceFeedbacks ->
                wordService
                    .getSentenceFeedbacksByIds(userSentenceFeedbacks.map { it.sentenceFeedbackId })
                    .sortedByDescending { it.createdAt }
            }

        return Response(feedbacks = sentenceFeedbacks)
    }
}

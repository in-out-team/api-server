package com.inout.apiserver.application.word

import com.inout.apiserver.base.enums.SentenceType
import com.inout.apiserver.base.service.openai.OpenAIService
import com.inout.apiserver.domain.user.User
import com.inout.apiserver.domain.word.SentenceFeedbackCreateObject
import com.inout.apiserver.domain.word.UserSentenceCreateObject
import com.inout.apiserver.domain.word.UserSentenceFeedbackCreateObject
import com.inout.apiserver.domain.word.WordService
import com.inout.apiserver.error.BadRequestException
import com.inout.apiserver.error.NotFoundException
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional
class GetWritingSentenceFeedbackApplication(
    private val wordService: WordService,
    private val openAIService: OpenAIService,
) {
    companion object {
        private const val MAX_ATTEMPTS = 3
    }

    data class Request(
        val user: User,
        val sentenceId: Long,
        /**
         * user's submitted answer for given writing practice sentence
         */
        val submittedContent: String,
    )

    data class Response(
        val feedback: String,
    )

    fun run(request: Request): Response {
        val sentence =
            wordService.getSentenceByIdAndType(request.sentenceId, SentenceType.WRITING)
                ?: run {
                    throw NotFoundException(
                        message = "Sentence not found for id: ${request.sentenceId}",
                        code = "SENTENCE_3",
                    )
                }

        if (sentence.content == request.submittedContent) {
            /**
             * TODO: need to check user's language settings.
             *  - currently, we only support Korean users learning English, thus hard coded.
             */
            return Response(feedback = "정답입니다!")
        }

        val userSentence =
            wordService.getUserSentenceBy(request.user.id, request.sentenceId)
                ?: run {
                    wordService.createUserSentence(
                        UserSentenceCreateObject(
                            userId = request.user.id,
                            wordDefinitionId = sentence.wordDefinitionId,
                            type = SentenceType.WRITING,
                            sentenceId = sentence.id,
                        ),
                    )
                }
        val userSentenceFeedbacks = wordService.getUserSentenceFeedbacks(userSentence.id)
        if (userSentenceFeedbacks.size >= MAX_ATTEMPTS) {
            throw BadRequestException(
                message = "Maximum attempts on getting writing sentence feedback has been reached",
                code = "SENTENCE_8",
            )
        }

        val sentenceFeedback =
            wordService.getSentenceFeedbackBy(request.sentenceId, request.submittedContent)
                ?: run {
                    val openAIWritingSentenceFeedbackResponse =
                        openAIService.fetchWritingSentenceFeedback(
                            originalContent = sentence.content,
                            userSubmittedContent = request.submittedContent,
                        )

                    wordService.createSentenceFeedback(
                        SentenceFeedbackCreateObject(
                            sentenceId = sentence.id,
                            submittedContent = request.submittedContent,
                            feedback = openAIWritingSentenceFeedbackResponse.feedback,
                        ),
                    )
                }

        wordService.createUserSentenceFeedback(
            UserSentenceFeedbackCreateObject(
                userSentenceId = userSentence.id,
                sentenceFeedbackId = sentenceFeedback.id,
            ),
        )

        return Response(feedback = sentenceFeedback.feedback)
    }
}

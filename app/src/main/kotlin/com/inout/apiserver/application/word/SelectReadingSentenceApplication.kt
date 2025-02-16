package com.inout.apiserver.application.word

import com.inout.apiserver.base.alias.SentenceId
import com.inout.apiserver.base.enums.SentenceType
import com.inout.apiserver.domain.word.UserSentenceCreateObject
import com.inout.apiserver.domain.word.WordService
import com.inout.apiserver.error.BadRequestException
import com.inout.apiserver.error.NotFoundException
import com.inout.apiserver.infrastructure.db.user.User
import com.inout.apiserver.infrastructure.db.word.UserSentence
import org.springframework.stereotype.Component

@Component
class SelectReadingSentenceApplication(
    private val wordService: WordService,
) {
    companion object {
        private const val MAX_SENTENCES_COUNT = 3
    }

    data class Request(
        val sentenceId: SentenceId,
        val user: User,
    )

    data class Response(
        val userSentence: UserSentence,
    )

    fun run(request: Request): Response {
        // FIXME: user needs to be studying given sentence
        val sentence =
            wordService.getSentenceByIdAndType(request.sentenceId, SentenceType.READING) ?: throw NotFoundException(
                message = "Sentence Not Found",
                code = "SENTENCE_1",
            )
        wordService
            .getUserSentencesBy(request.user.id!!, sentence.wordDefinitionId, SentenceType.READING)
            .let { userSentences ->
                if (userSentences.size >= MAX_SENTENCES_COUNT) {
                    throw BadRequestException(
                        message = "Maximum of $MAX_SENTENCES_COUNT sentences can be selected",
                        code = "SENTENCE_5",
                    )
                }
            }

        return Response(
            userSentence =
                wordService.createUserSentence(
                    UserSentenceCreateObject(
                        userId = request.user.id!!,
                        wordDefinitionId = sentence.wordDefinitionId,
                        type = SentenceType.READING,
                        sentenceId = sentence.id!!,
                    ),
                ),
        )
    }
}

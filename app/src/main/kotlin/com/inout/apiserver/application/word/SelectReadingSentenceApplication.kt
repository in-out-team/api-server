package com.inout.apiserver.application.word

import com.inout.apiserver.base.enums.SentenceType
import com.inout.apiserver.domain.word.MongoWordService
import com.inout.apiserver.error.BadRequestException
import com.inout.apiserver.error.NotFoundException
import com.inout.apiserver.infrastructure.mongo.user.MongoUser
import com.inout.apiserver.infrastructure.mongo.word.MongoUserSentence
import org.bson.types.ObjectId
import org.springframework.stereotype.Component

@Component
class SelectReadingSentenceApplication(
    private val wordService: MongoWordService,
) {
    companion object {
        private const val MAX_SENTENCES_COUNT = 3
    }

    data class Request(
        val sentenceId: ObjectId,
        val user: MongoUser,
    )

    data class Response(
        val userSentence: MongoUserSentence,
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
                    userId = request.user.id,
                    wordDefinitionId = sentence.wordDefinitionId,
                    type = SentenceType.READING,
                    sentenceId = sentence.id!!,
                ),
        )
    }
}

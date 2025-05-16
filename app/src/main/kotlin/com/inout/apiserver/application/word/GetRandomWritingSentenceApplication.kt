package com.inout.apiserver.application.word

import com.inout.apiserver.base.enums.SentenceType
import com.inout.apiserver.domain.word.MongoWordService
import com.inout.apiserver.error.BadRequestException
import com.inout.apiserver.error.NotFoundException
import com.inout.apiserver.infrastructure.mongo.user.MongoUser
import com.inout.apiserver.infrastructure.mongo.word.MongoSentence
import org.bson.types.ObjectId
import org.springframework.stereotype.Component

@Component
class GetRandomWritingSentenceApplication(
    private val wordService: MongoWordService,
) {
    companion object {
        private const val TARGET_SENTENCES_COUNT = 3
    }

    data class Request(
        val wordDefinitionId: ObjectId,
        val user: MongoUser,
    )

    data class Response(
        val sentence: MongoSentence,
    )

    fun run(request: Request): Response {
        val writingSentences =
            wordService.getSentencesByWordDefinitionIdAndType(request.wordDefinitionId, SentenceType.WRITING)
        if (writingSentences.isEmpty()) {
            throw NotFoundException(
                message = "Sentences not found for word definition id: ${request.wordDefinitionId}",
                code = "SENTENCE_1",
            )
        }

        val userSentences =
            wordService.getUserSentencesBy(request.user.id!!, request.wordDefinitionId, SentenceType.WRITING)
        if (userSentences.size >= TARGET_SENTENCES_COUNT) {
            throw BadRequestException(
                message = "User already has enough sentences",
                code = "SENTENCE_6",
            )
        }

        val sentences = writingSentences.filter { sentence -> userSentences.none { it.sentenceId == sentence.id } }
        if (sentences.isEmpty()) {
            throw BadRequestException(
                message = "All sentences are already selected",
                code = "SENTENCE_7",
            )
        }

        return Response(
            sentence = sentences.random(),
        )
    }
}

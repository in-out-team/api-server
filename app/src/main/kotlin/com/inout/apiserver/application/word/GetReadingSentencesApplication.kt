package com.inout.apiserver.application.word

import com.inout.apiserver.base.enums.SentenceType
import com.inout.apiserver.domain.word.MongoWordService
import com.inout.apiserver.error.NotFoundException
import com.inout.apiserver.infrastructure.mongo.user.MongoUser
import com.inout.apiserver.infrastructure.mongo.word.MongoSentence
import org.bson.types.ObjectId
import org.springframework.stereotype.Component

@Component
class GetReadingSentencesApplication(
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
        val selectedSentences: List<MongoSentence>,
        val unselectedSentences: List<MongoSentence>,
    )

    fun run(request: Request): Response {
        val sentences =
            wordService.getSentencesByWordDefinitionIdAndType(request.wordDefinitionId, SentenceType.READING)
        if (sentences.isEmpty()) {
            throw NotFoundException(
                message = "Sentences not found for word definition id: ${request.wordDefinitionId}",
                code = "SENTENCE_1",
            )
        }

        val userSentences =
            wordService
                .getUserSentencesBy(request.user.id!!, request.wordDefinitionId, SentenceType.READING)
                .toMutableList()
        if (userSentences.isEmpty()) {
            wordService
                .loadUserSentencesForReading(
                    request.user.id,
                    sentences
                        .shuffled()
                        .take(minOf(TARGET_SENTENCES_COUNT, (sentences.size / 3).coerceAtLeast(1))),
                ).also { userSentences.addAll(it) }
        }

        val (selectedSentences, unselectedSentences) =
            sentences.partition { sentence -> userSentences.any { it.sentenceId == sentence.id } }
        return Response(
            selectedSentences = selectedSentences,
            unselectedSentences = unselectedSentences,
        )
    }
}

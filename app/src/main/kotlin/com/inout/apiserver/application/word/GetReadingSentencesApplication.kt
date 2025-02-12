package com.inout.apiserver.application.word

import com.inout.apiserver.base.alias.WordDefinitionId
import com.inout.apiserver.base.enums.SentenceType
import com.inout.apiserver.domain.word.WordService
import com.inout.apiserver.error.NotFoundException
import com.inout.apiserver.infrastructure.db.user.User
import com.inout.apiserver.infrastructure.db.word.Sentence
import org.springframework.stereotype.Component

@Component
class GetReadingSentencesApplication(
    private val wordService: WordService,
) {
    companion object {
        private const val TARGET_SENTENCES_COUNT = 3
    }

    data class Request(
        val wordDefinitionId: WordDefinitionId,
        val user: User,
    )

    data class Response(
        val selectedSentences: List<Sentence>,
        val unselectedSentences: List<Sentence>,
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
                    request.user.id!!,
                    sentences
                        .shuffled()
                        .take(minOf(TARGET_SENTENCES_COUNT, (sentences.size / 3).coerceAtLeast(1))),
                ).also { userSentences.addAll(it) }
        }

        val (selectedSentences, unselectedSentences) =
            sentences.partition { sentence -> userSentences.any { it.id == sentence.id } }
        return Response(
            selectedSentences = selectedSentences,
            unselectedSentences = unselectedSentences,
        )
    }
}

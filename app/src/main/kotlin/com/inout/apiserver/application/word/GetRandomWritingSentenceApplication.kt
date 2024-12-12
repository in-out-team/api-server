package com.inout.apiserver.application.word

import com.inout.apiserver.base.enums.SentenceType
import com.inout.apiserver.domain.user.User
import com.inout.apiserver.domain.word.Sentence
import com.inout.apiserver.domain.word.WordService
import com.inout.apiserver.error.BadRequestException
import com.inout.apiserver.error.NotFoundException
import org.springframework.stereotype.Component

@Component
class GetRandomWritingSentenceApplication(
    private val wordService: WordService,
) {
    companion object {
        private const val TARGET_SENTENCES_COUNT = 3
    }

    fun run(
        user: User,
        wordDefinitionId: Long,
    ): Sentence {
        val writingSentences = wordService.getSentencesByWordDefinitionIdAndType(wordDefinitionId, SentenceType.WRITING)
        if (writingSentences.isEmpty()) {
            throw NotFoundException(
                message = "Sentences not found for word definition id: $wordDefinitionId",
                code = "SENTENCE_1",
            )
        }

        val userSentences = wordService.getUserSentencesBy(user.id, wordDefinitionId, SentenceType.WRITING)
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

        return sentences.random()
    }
}

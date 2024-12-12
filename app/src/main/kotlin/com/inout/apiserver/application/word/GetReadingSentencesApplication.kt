package com.inout.apiserver.application.word

import com.inout.apiserver.base.enums.SentenceType
import com.inout.apiserver.domain.user.User
import com.inout.apiserver.domain.word.Sentence
import com.inout.apiserver.domain.word.WordService
import com.inout.apiserver.error.NotFoundException
import org.springframework.stereotype.Component

@Component
class GetReadingSentencesApplication(
    private val wordService: WordService,
) {
    companion object {
        private const val TARGET_SENTENCES_COUNT = 3
    }

    /**
     * return sentences of first: selected by user, second: not selected by user
     */
    fun run(
        wordDefinitionId: Long,
        user: User,
    ): Pair<List<Sentence>, List<Sentence>> {
        val sentences = wordService.getSentencesByWordDefinitionIdAndType(wordDefinitionId, SentenceType.READING)
        if (sentences.isEmpty()) {
            throw NotFoundException(
                message = "Sentences not found for word definition id: $wordDefinitionId",
                code = "SENTENCE_1",
            )
        }

        val userSentences =
            wordService.getUserSentencesBy(user.id, wordDefinitionId, SentenceType.READING).toMutableList()
        if (userSentences.isEmpty()) {
            wordService
                .loadUserSentencesForReading(
                    user.id,
                    sentences
                        .shuffled()
                        .take(minOf(TARGET_SENTENCES_COUNT, (sentences.size / 3).coerceAtLeast(1))),
                ).also { userSentences.addAll(it) }
        }

        return sentences.partition { sentence -> userSentences.any { it.id == sentence.id } }
    }
}

package com.inout.apiserver.application.word

import com.inout.apiserver.domain.user.User
import com.inout.apiserver.domain.word.Sentence
import com.inout.apiserver.domain.word.WordService
import com.inout.apiserver.error.NotFoundException
import org.springframework.stereotype.Component

@Component
class ReadSentencesApplication(
    private val wordService: WordService,
) {
    /**
     * return sentences of first: selected by user, second: not selected by user
     */
    fun run(
        wordDefinitionId: Long,
        user: User,
    ): Pair<List<Sentence>, List<Sentence>> {
        val sentences = wordService.getSentencesByWordDefinitionId(wordDefinitionId).toSet()
        if (sentences.isEmpty()) {
            throw NotFoundException(
                message = "Sentences not found for word definition id: $wordDefinitionId",
                code = "WORD_3",
            )
        }

        val userSentences = wordService.getUserSentencesBy(user.id, wordDefinitionId)
        return sentences.partition { sentence -> userSentences.any { it.sentenceId == sentence.id } }
    }
}

package com.inout.apiserver.application.word

import com.inout.apiserver.domain.word.Sentence
import com.inout.apiserver.domain.word.WordService
import com.inout.apiserver.error.NotFoundException
import org.springframework.stereotype.Component

@Component
class ReadSentencesApplication(
    private val wordService: WordService,
) {
    fun run(wordDefinitionId: Long): List<Sentence> {
        val sentences = wordService.getSentencesByWordDefinitionId(wordDefinitionId)
        if (sentences.isEmpty()) {
            throw NotFoundException(
                message = "Sentences not found for word definition id: $wordDefinitionId",
                code = "WORD_3",
            )
        }
        return sentences
    }
}

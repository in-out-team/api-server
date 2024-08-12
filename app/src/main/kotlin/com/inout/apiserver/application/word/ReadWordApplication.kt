package com.inout.apiserver.application.word

import com.inout.apiserver.domain.word.WordService
import com.inout.apiserver.error.NotFoundException
import com.inout.apiserver.interfaces.web.v1.request.ReadWordRequest
import com.inout.apiserver.interfaces.web.v1.response.WordResponse
import org.springframework.stereotype.Component

@Component
class ReadWordApplication(
    private val wordService: WordService,
) {
    fun run(request: ReadWordRequest): WordResponse {
        val word = wordService.getWordByNameAndFromLanguageAndToLanguage(
            name = request.name,
            fromLanguage = request.fromLanguage,
            toLanguage = request.toLanguage
        ) ?: throw NotFoundException(message = "Word not found", code = "WORD_2")

        return WordResponse.of(word)
    }
}

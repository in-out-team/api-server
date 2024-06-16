package com.inout.apiserver.application.word

import com.inout.apiserver.domain.word.WordService
import com.inout.apiserver.error.NotFoundException
import com.inout.apiserver.base.enums.LanguageType
import com.inout.apiserver.interfaces.web.v1.response.WordResponse
import org.springframework.stereotype.Component

@Component
class ReadOrCreateWordApplication(
    private val wordService: WordService
) {
    fun run(name: String, language: LanguageType): WordResponse {
        val word = wordService.getWordByNameAndLanguage(name, language)
            ?: throw NotFoundException("Word not found", "WORD_1")
        // TODO: if word is not present, hit external API to get the word

        return WordResponse.of(word)
    }
}
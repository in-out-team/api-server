package com.inout.apiserver.application.word

import com.inout.apiserver.base.enums.LexicalCategoryType
import com.inout.apiserver.base.service.openai.OpenAIService
import com.inout.apiserver.domain.word.WordCreateObject
import com.inout.apiserver.domain.word.WordDefinitionCreateObject
import com.inout.apiserver.domain.word.WordService
import com.inout.apiserver.error.ConflictException
import com.inout.apiserver.interfaces.web.v1.request.CreateWordRequest
import com.inout.apiserver.interfaces.web.v1.response.WordResponse
import org.springframework.stereotype.Component

@Component
class CreateWordApplication(
    private val wordService: WordService,
    private val openAIService: OpenAIService
) {
    // TODO: need to accept user
    fun run(request: CreateWordRequest): WordResponse {
        wordService.getWordByNameAndFromLanguageAndToLanguage(
            name = request.name,
            fromLanguage = request.fromLanguage,
            toLanguage = request.toLanguage
        )?.let {
            throw ConflictException(message = "Word already exists", code = "WORD_1")
        }

        val openAIWordDefinitionResponse = openAIService.fetchWordDefinition(
            word = request.name,
            fromLanguage = request.fromLanguage.name.lowercase().replaceFirstChar { it.uppercase() },
            toLanguage = request.toLanguage.name.lowercase().replaceFirstChar { it.uppercase() }
        )

        val wordCreateObject = WordCreateObject(
            name = request.name,
            fromLanguage = request.fromLanguage,
            toLanguage = request.toLanguage,
            definitions = openAIWordDefinitionResponse.definitions.map {
                WordDefinitionCreateObject(
                    lexicalCategory = LexicalCategoryType.of(it.type),
                    meaning = it.definition,
                    preContext = it.preContext
                )
            }
        )
        return WordResponse.of(wordService.createWord(wordCreateObject))
    }
}
package com.inout.apiserver.application.word

import com.inout.apiserver.base.enums.LanguageType
import com.inout.apiserver.base.enums.LexicalCategoryType
import com.inout.apiserver.base.service.openai.OpenAIService
import com.inout.apiserver.base.service.openai.dto.OpenAIWordDefinitionResponse
import com.inout.apiserver.domain.word.WordCreateObject
import com.inout.apiserver.domain.word.WordDefinitionCreateObject
import com.inout.apiserver.domain.word.WordService
import com.inout.apiserver.error.BadRequestException
import com.inout.apiserver.error.ConflictException
import com.inout.apiserver.infrastructure.db.word.Word
import org.springframework.stereotype.Component

@Component
class CreateWordApplication(
    private val wordService: WordService,
    private val openAIService: OpenAIService,
) {
    data class Request(
        val name: String,
        val fromLanguage: LanguageType,
        val toLanguage: LanguageType,
    )

    data class Response(
        val word: Word,
    )

    fun run(request: Request): Response {
        validateWordDoesNotExist(request.name, request.fromLanguage, request.toLanguage)

        val fromLanguage = request.fromLanguage.format()
        val toLanguage = request.toLanguage.format()
        val openAIWordDefinitionResponse = fetchValidatedDefinitions(request, fromLanguage, toLanguage)
        if (openAIWordDefinitionResponse.definitions.isEmpty()) {
            throw BadRequestException(message = "Valid Word Definition not found", code = "WORD_3")
        }

        val wordCreateObject = buildWordCreateObject(request, openAIWordDefinitionResponse)
        val word = wordService.createWord(wordCreateObject)

        return Response(word)
    }

    private fun validateWordDoesNotExist(
        name: String,
        fromLanguage: LanguageType,
        toLanguage: LanguageType,
    ) {
        wordService
            .getWordByNameAndFromLanguageAndToLanguage(
                name = name,
                fromLanguage = fromLanguage,
                toLanguage = toLanguage,
            )?.let {
                throw ConflictException(message = "Word already exists", code = "WORD_1")
            }
    }

    private fun LanguageType.format(): String = name.lowercase().replaceFirstChar { it.uppercase() }

    private fun fetchValidatedDefinitions(
        request: Request,
        fromLanguage: String,
        toLanguage: String,
    ): OpenAIWordDefinitionResponse {
        val openAIWordDefinitionResponse =
            openAIService.fetchWordDefinition(
                word = request.name,
                fromLanguage = fromLanguage,
                toLanguage = toLanguage,
            )
        if (openAIWordDefinitionResponse.definitions.isEmpty()) {
            return openAIWordDefinitionResponse
        }

        return openAIService.validateAndTrimWordDefinition(
            word = request.name,
            fromLanguage = fromLanguage,
            toLanguage = toLanguage,
            definitions =
                openAIWordDefinitionResponse.definitions.map {
                    Triple(it.type, it.definition, it.preContext)
                },
        )
    }

    private fun buildWordCreateObject(
        request: Request,
        openAIWordDefinitionResponse: OpenAIWordDefinitionResponse,
    ): WordCreateObject =
        WordCreateObject(
            name = request.name,
            fromLanguage = request.fromLanguage,
            toLanguage = request.toLanguage,
            definitions =
                openAIWordDefinitionResponse.definitions.map {
                    WordDefinitionCreateObject(
                        lexicalCategory = LexicalCategoryType.of(it.type),
                        meaning = it.definition,
                        preContext = it.preContext,
                    )
                },
        )
}

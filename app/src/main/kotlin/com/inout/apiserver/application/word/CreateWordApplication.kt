package com.inout.apiserver.application.word

import com.inout.apiserver.base.enums.LanguageType
import com.inout.apiserver.base.enums.LexicalCategoryType
import com.inout.apiserver.base.service.DictionaryService
import com.inout.apiserver.domain.word.WordCreateObject
import com.inout.apiserver.domain.word.WordDefinitionCreateObject
import com.inout.apiserver.domain.word.WordService
import com.inout.apiserver.domain.word.WordWithDefinitions
import com.inout.apiserver.error.BadRequestException
import com.inout.apiserver.error.ConflictException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component

@Component
class CreateWordApplication(
    private val wordService: WordService,
    private val dictionaryService: DictionaryService,
) {
    data class Request(
        val name: String,
        val fromLanguage: LanguageType,
        val toLanguage: LanguageType,
    )

    data class Response(
        val word: WordWithDefinitions,
    )

    /**
     * TODO: 3rd party service calls should be logged
     * - who called the service
     * - what was the request
     * - what was the response
     *   - what was the status code
     *   - what was the error message
     * - how long did it take
     * - what was the cost (API token usage in this case)
     */
    fun run(request: Request): Response {
        validateWordDoesNotExist(request.name, request.fromLanguage, request.toLanguage)

        val definitions = fetchValidatedDefinitions(request, request.fromLanguage, request.toLanguage)
        if (definitions.isEmpty()) {
            throw BadRequestException(message = "Valid Word Definition not found", code = "WORD_3")
        }

        val wordCreateObject = buildWordCreateObject(request, definitions)
        val word = wordService.createWord(wordCreateObject)

        return Response(word)
    }

    private fun validateWordDoesNotExist(
        name: String,
        fromLanguage: LanguageType,
        toLanguage: LanguageType,
    ) {
        wordService
            .getWordWithDefinitionsBy(
                name = name,
                fromLanguage = fromLanguage,
                toLanguage = toLanguage,
            )?.let {
                throw ConflictException(message = "Word already exists", code = "WORD_1")
            }
    }

    private fun fetchValidatedDefinitions(
        request: Request,
        fromLanguage: LanguageType,
        toLanguage: LanguageType,
    ): List<DictionaryService.Definition> {
        val definitions =
            dictionaryService
                .fetchWordDefinitions(
                    word = request.name,
                    fromLanguage = fromLanguage,
                    toLanguage = toLanguage,
                ).takeIf { it.isNotEmpty() }
                ?: return emptyList()

        return runBlocking {
            definitions
                .map { definition ->
                    async {
                        dictionaryService.validateWordDefinition(
                            word = request.name,
                            fromLanguage = fromLanguage,
                            toLanguage = toLanguage,
                            definition = definition,
                        ) to definition
                    }
                }.awaitAll()
                .filter { it.first }
                .map { it.second }
        }
    }

    private fun buildWordCreateObject(
        request: Request,
        definitions: List<DictionaryService.Definition>,
    ): WordCreateObject =
        WordCreateObject(
            name = request.name,
            fromLanguage = request.fromLanguage,
            toLanguage = request.toLanguage,
            definitions =
                definitions.map {
                    WordDefinitionCreateObject(
                        lexicalCategory = LexicalCategoryType.of(it.lexicalCategory),
                        meaning = it.definition,
                        preContext = it.preContext,
                    )
                },
        )
}

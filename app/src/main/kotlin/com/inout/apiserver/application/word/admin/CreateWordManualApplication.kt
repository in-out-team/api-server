package com.inout.apiserver.application.word.admin

import com.inout.apiserver.base.enums.LanguageType
import com.inout.apiserver.domain.word.MongoWordService
import com.inout.apiserver.domain.word.WordCreateObject
import com.inout.apiserver.domain.word.WordWithDefinitions
import com.inout.apiserver.error.ConflictException
import org.springframework.stereotype.Component

@Component
class CreateWordManualApplication(
    private val wordService: MongoWordService,
) {
    data class Request(
        val name: String,
        val fromLanguage: LanguageType,
        val toLanguage: LanguageType,
    )

    data class Response(
        val word: WordWithDefinitions,
    )

    fun run(request: Request): Response {
        validateWordDoesNotExist(
            name = request.name,
            fromLanguage = request.fromLanguage,
            toLanguage = request.toLanguage,
        )

        return Response(word = createWord(request))
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

    private fun createWord(request: Request): WordWithDefinitions {
        val createdWord =
            wordService.createWord(
                WordCreateObject(
                    name = request.name,
                    fromLanguage = request.fromLanguage,
                    toLanguage = request.toLanguage,
                    definitions = emptyList(),
                ),
            )
        return createdWord
    }
}

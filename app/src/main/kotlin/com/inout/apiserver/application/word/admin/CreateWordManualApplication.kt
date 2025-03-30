package com.inout.apiserver.application.word.admin

import com.inout.apiserver.base.enums.LanguageType
import com.inout.apiserver.domain.word.WordCreateObject
import com.inout.apiserver.domain.word.WordService
import com.inout.apiserver.error.ConflictException
import com.inout.apiserver.infrastructure.db.word.Word
import org.springframework.stereotype.Component

@Component
class CreateWordManualApplication(
    private val wordService: WordService,
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
            .getWordByNameAndFromLanguageAndToLanguage(
                name = name,
                fromLanguage = fromLanguage,
                toLanguage = toLanguage,
            )?.let {
                throw ConflictException(message = "Word already exists", code = "WORD_1")
            }
    }

    private fun createWord(request: Request): Word {
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

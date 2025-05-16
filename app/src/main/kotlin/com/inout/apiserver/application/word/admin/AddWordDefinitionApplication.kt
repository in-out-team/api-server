package com.inout.apiserver.application.word.admin

import com.inout.apiserver.base.enums.LexicalCategoryType
import com.inout.apiserver.base.enums.StatusType
import com.inout.apiserver.domain.word.WordService
import com.inout.apiserver.domain.word.WordWithDefinitions
import com.inout.apiserver.error.ConflictException
import com.inout.apiserver.error.NotFoundException
import org.bson.types.ObjectId
import org.springframework.stereotype.Component

@Component
class AddWordDefinitionApplication(
    private val wordService: WordService,
) {
    data class Request(
        val wordId: ObjectId,
        val lexicalCategory: LexicalCategoryType,
        val meaning: String,
        val preContext: String,
    )

    data class Response(
        val word: WordWithDefinitions,
    )

    fun run(request: Request): Response {
        val word = findAndValidateWord(request)
        val updatedWord = updateWord(word, request)

        return Response(word = updatedWord)
    }

    private fun findAndValidateWord(request: Request): WordWithDefinitions {
        val word =
            wordService.getWordWithDefinitionsBy(request.wordId) ?: throw NotFoundException(
                message = "Word not found",
                code = "WORD_4",
            )
        word.definitions.forEach { wordDefinition ->
            if (
                wordDefinition.status != StatusType.REMOVED &&
                wordDefinition.meaning == request.meaning &&
                wordDefinition.lexicalCategory == request.lexicalCategory
            ) {
                throw ConflictException(
                    message = "Word definition already exists",
                    code = "WORD_5",
                )
            }
        }
        return word
    }

    private fun updateWord(
        word: WordWithDefinitions,
        request: Request,
    ) = wordService.addWordDefinition(
        word = word,
        lexicalCategory = request.lexicalCategory,
        meaning = request.meaning,
        preContext = request.preContext,
    )
}

package com.inout.apiserver.application.word.admin

import com.inout.apiserver.base.alias.WordId
import com.inout.apiserver.base.enums.LexicalCategoryType
import com.inout.apiserver.base.enums.StatusType
import com.inout.apiserver.domain.word.WordDefinitionCreateObject
import com.inout.apiserver.domain.word.WordService
import com.inout.apiserver.error.ConflictException
import com.inout.apiserver.error.NotFoundException
import com.inout.apiserver.infrastructure.db.word.Word
import com.inout.apiserver.infrastructure.db.word.WordDefinition
import com.inout.apiserver.infrastructure.db.word.WordRepository
import org.springframework.stereotype.Component

@Component
class AddWordDefinitionApplication(
    private val wordService: WordService,
    // injected wordRepository here to avoid adding update on word method on commonly used WordService
    private val wordRepository: WordRepository,
) {
    data class Request(
        val wordId: WordId,
        val lexicalCategory: LexicalCategoryType,
        val meaning: String,
        val preContext: String,
    )

    data class Response(
        val word: Word,
    )

    fun run(request: Request): Response {
        val word = findAndValidateWord(request)
        val updatedWord = updateWord(word, request)

        return Response(word = updatedWord)
    }

    private fun findAndValidateWord(request: Request): Word {
        val word =
            wordService.getWordById(request.wordId) ?: throw NotFoundException(
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
        word: Word,
        request: Request,
    ): Word {
        word.addDefinitions(
            listOf(
                WordDefinition.fromCreateObject(
                    createObject =
                        WordDefinitionCreateObject(
                            lexicalCategory = request.lexicalCategory,
                            meaning = request.meaning,
                            preContext = request.preContext,
                        ),
                    word = word,
                ),
            ),
        )

        val updatedWord = wordRepository.save(word)
        return updatedWord
    }
}

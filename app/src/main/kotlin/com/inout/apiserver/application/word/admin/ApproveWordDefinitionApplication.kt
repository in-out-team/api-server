package com.inout.apiserver.application.word.admin

import com.inout.apiserver.base.alias.WordDefinitionId
import com.inout.apiserver.base.alias.WordId
import com.inout.apiserver.base.enums.StatusType
import com.inout.apiserver.domain.word.WordService
import com.inout.apiserver.error.BadRequestException
import com.inout.apiserver.error.NotFoundException
import com.inout.apiserver.infrastructure.db.word.Word
import com.inout.apiserver.infrastructure.db.word.WordRepository
import org.springframework.stereotype.Component

@Component
class ApproveWordDefinitionApplication(
    private val wordService: WordService,
    // injected wordRepository here to avoid adding update on word method on commonly used WordService
    private val wordRepository: WordRepository,
) {
    data class Request(
        val wordId: WordId,
        val wordDefinitionId: WordDefinitionId,
    )

    data class Response(
        val word: Word,
    )

    fun run(request: Request): Response {
        val word = findAndValidateWord(request)
        val updatedWord = approveWordDefinition(word, request)

        return Response(word = updatedWord)
    }

    private fun findAndValidateWord(request: Request): Word {
        val word = wordService.getWordById(request.wordId)
        val wordDefinition =
            word?.definitions?.find { it.id == request.wordDefinitionId }
                ?: throw NotFoundException(
                    message = "Word definition not found",
                    code = "WORD_4",
                )
        if (wordDefinition.status != StatusType.PENDING) {
            throw BadRequestException(
                message = "Cannot approve word definition that is not pending",
                code = "WORD_6",
            )
        }

        return word
    }

    private fun approveWordDefinition(
        word: Word,
        request: Request,
    ): Word {
        word.approveDefinition(request.wordDefinitionId)
        return wordRepository.save(word)
    }
}

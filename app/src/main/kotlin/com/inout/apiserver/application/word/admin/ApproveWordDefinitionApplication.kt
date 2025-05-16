package com.inout.apiserver.application.word.admin

import com.inout.apiserver.base.enums.StatusType
import com.inout.apiserver.domain.word.WordService
import com.inout.apiserver.domain.word.WordWithDefinitions
import com.inout.apiserver.error.BadRequestException
import com.inout.apiserver.error.NotFoundException
import org.bson.types.ObjectId
import org.springframework.stereotype.Component

@Component
class ApproveWordDefinitionApplication(
    private val wordService: WordService,
) {
    data class Request(
        val wordId: ObjectId,
        val wordDefinitionId: ObjectId,
    )

    data class Response(
        val word: WordWithDefinitions,
    )

    fun run(request: Request): Response {
        val word = findAndValidateWord(request)
        val updatedWord = approveWordDefinition(word = word, wordDefinitionId = request.wordDefinitionId)

        return Response(word = updatedWord)
    }

    private fun findAndValidateWord(request: Request): WordWithDefinitions {
        val word = wordService.getWordWithDefinitionsBy(request.wordId)
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
        word: WordWithDefinitions,
        wordDefinitionId: ObjectId,
    ): WordWithDefinitions = wordService.approveWordDefinition(word = word, wordDefinitionId = wordDefinitionId)
}

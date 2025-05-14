package com.inout.apiserver.application.word.admin

import com.inout.apiserver.base.enums.StatusType
import com.inout.apiserver.domain.study.MongoStudyService
import com.inout.apiserver.domain.word.MongoWordService
import com.inout.apiserver.domain.word.WordWithDefinitions
import com.inout.apiserver.error.BadRequestException
import com.inout.apiserver.error.NotFoundException
import org.bson.types.ObjectId
import org.springframework.stereotype.Component

@Component
class DeleteWordDefinitionApplication(
    private val wordService: MongoWordService,
    private val studyService: MongoStudyService,
) {
    data class Request(
        val wordId: ObjectId,
        val wordDefinitionId: ObjectId,
    )

    data class Response(
        val word: WordWithDefinitions,
    )

    fun run(request: Request): Response {
        val word = findAndValidateWordDefinition(request)
        val updatedWord = deleteWordDefinition(word, request.wordDefinitionId)
        return Response(word = updatedWord)
    }

    private fun findAndValidateWordDefinition(request: Request): WordWithDefinitions {
        val word = wordService.getWordWithDefinitionsBy(request.wordId)
        val wordDefinition =
            word?.let {
                it.definitions.find { definition -> definition.id == request.wordDefinitionId }
            } ?: throw NotFoundException(
                message = "Word definition not found",
                code = "WORD_4",
            )

        val deletable =
            when (wordDefinition.status) {
                // if it is pending, it can be deleted
                StatusType.PENDING -> true
                // if it is approved but not used, it can be deleted
                StatusType.LIVE -> !studyService.existsStudyByWordDefinitionId(request.wordDefinitionId)
                // otherwise(if already deleted), it cannot be deleted
                else -> false
            }

        if (!deletable) {
            throw BadRequestException(
                message = "Cannot delete word definition that is not pending or not used",
                code = "WORD_7",
            )
        }

        return word
    }

    private fun deleteWordDefinition(
        word: WordWithDefinitions,
        wordDefinitionId: ObjectId,
    ): WordWithDefinitions =
        wordService.removeWordDefinition(
            word = word,
            wordDefinitionId = wordDefinitionId,
        )
}

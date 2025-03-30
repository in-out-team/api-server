package com.inout.apiserver.application.word.admin

import com.inout.apiserver.base.alias.WordDefinitionId
import com.inout.apiserver.base.alias.WordId
import com.inout.apiserver.base.enums.StatusType
import com.inout.apiserver.domain.study.StudyService
import com.inout.apiserver.domain.word.WordService
import com.inout.apiserver.error.BadRequestException
import com.inout.apiserver.error.NotFoundException
import com.inout.apiserver.infrastructure.db.word.Word
import com.inout.apiserver.infrastructure.db.word.WordRepository
import org.springframework.stereotype.Component

@Component
class DeleteWordDefinitionApplication(
    private val wordService: WordService,
    private val studyService: StudyService,
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
        val word = findAndValidateWordDefinition(request)
        val updatedWord = deleteWordDefinition(word, request)
        return Response(word = updatedWord)
    }

    private fun findAndValidateWordDefinition(request: Request): Word {
        val word = wordService.getWordById(request.wordId)
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
        word: Word,
        request: Request,
    ): Word {
        word.removeDefinition(request.wordDefinitionId)
        wordRepository.save(word)
        return word
    }
}

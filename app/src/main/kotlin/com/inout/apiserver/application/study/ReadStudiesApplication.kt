package com.inout.apiserver.application.study

import com.inout.apiserver.domain.study.StudyService
import com.inout.apiserver.domain.study.StudyWord
import com.inout.apiserver.domain.word.WordService
import com.inout.apiserver.domain.word.WordWithDefinitions
import com.inout.apiserver.error.InternalServerErrorException
import com.inout.apiserver.infrastructure.mongo.user.User
import org.bson.types.ObjectId
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component

@Component
class ReadStudiesApplication(
    private val studyService: StudyService,
    private val wordService: WordService,
) {
    data class Request(
        val user: User,
        val wordNamePrefix: String?,
        val pageable: Pageable,
    )

    data class Response(
        val totalCount: Long,
        val studies: List<StudyWord>,
    )

    fun run(request: Request): Response {
        val studies = studyService.getAllByUserId(request.user.id!!, request.wordNamePrefix, request.pageable)
        val wordDefinitionIds = studies.content.map { it.wordDefinitionId }
        val words = wordService.getWordsByLiveWordDefinitionIds(wordDefinitionIds)
        val wordByDefinitionIdMap = mutableMapOf<ObjectId, WordWithDefinitions>()
        words.forEach { word ->
            word.definitions.forEach { definition -> wordByDefinitionIdMap[definition.id!!] = word }
        }

        return Response(
            totalCount = studies.totalElements,
            studies =
                studies.content.map { study ->
                    StudyWord(
                        study = study,
                        word =
                            wordByDefinitionIdMap[study.wordDefinitionId]
                                ?: throw InternalServerErrorException(
                                    message = "Data Integrity Error",
                                    code = "STUDY_2",
                                ),
                    )
                },
        )
    }
}

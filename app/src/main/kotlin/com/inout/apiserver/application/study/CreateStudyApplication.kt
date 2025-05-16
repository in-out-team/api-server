package com.inout.apiserver.application.study

import com.inout.apiserver.domain.study.StudyService
import com.inout.apiserver.domain.study.StudyWord
import com.inout.apiserver.domain.word.WordService
import com.inout.apiserver.infrastructure.mongo.user.User
import org.bson.types.ObjectId
import org.springframework.stereotype.Component

@Component
class CreateStudyApplication(
    private val studyService: StudyService,
    private val wordService: WordService,
) {
    data class Request(
        val user: User,
        val wordDefinitionId: ObjectId,
    )

    data class Response(
        val studyWord: StudyWord,
    )

    fun run(request: Request): Response {
        val word = wordService.getWordByLiveWordDefinitionId(request.wordDefinitionId)
        val createdStudy =
            studyService.createStudy(
                userId = request.user.id!!,
                wordDefinitionId = request.wordDefinitionId,
            )

        return Response(
            studyWord =
                StudyWord(
                    word = word,
                    study = createdStudy,
                ),
        )
    }
}

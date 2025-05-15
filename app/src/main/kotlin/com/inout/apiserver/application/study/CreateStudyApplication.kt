package com.inout.apiserver.application.study

import com.inout.apiserver.domain.study.MongoStudyService
import com.inout.apiserver.domain.study.MongoStudyWord
import com.inout.apiserver.domain.word.MongoWordService
import com.inout.apiserver.infrastructure.mongo.user.MongoUser
import org.bson.types.ObjectId
import org.springframework.stereotype.Component

@Component
class CreateStudyApplication(
    private val studyService: MongoStudyService,
    private val wordService: MongoWordService,
) {
    data class Request(
        val user: MongoUser,
        val wordDefinitionId: ObjectId,
    )

    data class Response(
        val studyWord: MongoStudyWord,
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
                MongoStudyWord(
                    word = word,
                    study = createdStudy,
                ),
        )
    }
}

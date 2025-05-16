package com.inout.apiserver.application.study

import com.inout.apiserver.base.enums.FsrsCardRating
import com.inout.apiserver.domain.study.MongoStudyService
import com.inout.apiserver.domain.study.MongoStudyWord
import com.inout.apiserver.domain.word.MongoWordService
import com.inout.apiserver.error.NotFoundException
import com.inout.apiserver.infrastructure.mongo.user.MongoUser
import org.bson.types.ObjectId
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional
class RateStudyWordApplication(
    private val studyService: MongoStudyService,
    private val wordService: MongoWordService,
) {
    data class Request(
        val user: MongoUser,
        val dailyStudySetId: ObjectId,
        val studyId: ObjectId,
        val rating: FsrsCardRating,
    )

    data class Response(
        val studyWord: MongoStudyWord,
    )

    fun run(request: Request): Response {
        val dailyStudySet = studyService.getDailyStudySetById(request.dailyStudySetId)
        if (dailyStudySet == null || dailyStudySet.userId != request.user.id) {
            throw NotFoundException(message = "Daily study set not found", code = "STUDY_4")
        }
        val studies = studyService.getStudiesByDailyStudySet(dailyStudySet, request.user)
        val targetStudy =
            studies.find { it.id == request.studyId && it.userId == request.user.id }
                ?: throw NotFoundException(
                    message = "studyId of ${request.studyId} not found in dailyStudySetId of ${request.dailyStudySetId}",
                    code = "STUDY_2",
                )
        val updatedStudy = studyService.updateStudy(targetStudy.rate(request.rating))

        studyService.addStudyToDailyStudySet(dailyStudySet, updatedStudy)

        val word = wordService.getWordByLiveWordDefinitionId(updatedStudy.wordDefinitionId)
        return Response(studyWord = MongoStudyWord(study = updatedStudy, word = word))
    }
}

package com.inout.apiserver.application.study

import com.inout.apiserver.base.alias.DailyStudySetId
import com.inout.apiserver.base.alias.StudyId
import com.inout.apiserver.base.enums.FsrsCardRating
import com.inout.apiserver.domain.study.StudyService
import com.inout.apiserver.domain.study.StudyWord
import com.inout.apiserver.domain.word.WordService
import com.inout.apiserver.error.NotFoundException
import com.inout.apiserver.infrastructure.db.user.User
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional
class RateStudyWordApplication(
    private val studyService: StudyService,
    private val wordService: WordService,
) {
    data class Request(
        val user: User,
        val dailyStudySetId: DailyStudySetId,
        val studyId: StudyId,
        val rating: FsrsCardRating,
    )

    data class Response(
        val studyWord: StudyWord,
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
        return Response(studyWord = StudyWord(study = updatedStudy, word = word))
    }
}

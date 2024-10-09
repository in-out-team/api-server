package com.inout.apiserver.application.study

import com.inout.apiserver.base.enums.FsrsCardRating
import com.inout.apiserver.domain.study.StudyService
import com.inout.apiserver.domain.study.StudyWord
import com.inout.apiserver.domain.word.WordService
import com.inout.apiserver.error.NotFoundException
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional
class RateStudyWordApplication(
    private val studyService: StudyService,
    private val wordService: WordService,
) {
    data class Result(
        val studyWord: StudyWord,
    )

    fun run(
        userId: Long,
        dailyStudySetId: Long,
        studyId: Long,
        rating: FsrsCardRating,
    ): Result {
        val dailyStudySet = studyService.getDailyStudySetById(dailyStudySetId)
        if (dailyStudySet == null || dailyStudySet.userId != userId) {
            throw NotFoundException(message = "Daily study set not found", code = "STUDY_4")
        }
        val studies = studyService.getStudiesByDailyStudySet(dailyStudySet)
        val targetStudy =
            studies.find { it.id == studyId && it.userId == userId }
                ?: throw NotFoundException(
                    message = "studyId of $studyId not found in dailyStudySetId of $dailyStudySetId",
                    code = "STUDY_2",
                )
        val updatedStudy = studyService.rateStudy(targetStudy, rating)
        studyService.addStudyToDailyStudySet(dailyStudySet, updatedStudy)

        val word = wordService.getWordByWordDefinitionId(updatedStudy.wordDefinitionId)
        return Result(studyWord = StudyWord(study = updatedStudy, word = word))
    }
}

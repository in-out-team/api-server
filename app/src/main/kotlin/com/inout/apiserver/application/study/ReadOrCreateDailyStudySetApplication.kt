package com.inout.apiserver.application.study

import com.inout.apiserver.domain.study.DailyStudySet
import com.inout.apiserver.domain.study.DailyStudySetCreateObject
import com.inout.apiserver.domain.study.StudyService
import com.inout.apiserver.domain.study.StudyWord
import com.inout.apiserver.domain.word.Word
import com.inout.apiserver.domain.word.WordService
import com.inout.apiserver.error.BadRequestException
import com.inout.apiserver.error.NotFoundException
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset

@Component
class ReadOrCreateDailyStudySetApplication(
    private val studyService: StudyService,
    private val wordService: WordService,
) {
    companion object {
        const val DEFAULT_STUDY_SET_SIZE = 20 // TODO: later fix with user's settings
    }

    data class Result(
        val dailyStudySet: DailyStudySet,
        val studyWords: List<StudyWord>,
    )

    fun run(
        userId: Long,
        date: LocalDate,
    ): Result {
        val now = LocalDate.now()
        if (date.isAfter(now)) {
            throw BadRequestException(message = "Cannot request future daily study set", code = "STUDY_3")
        }

        var dailyStudySet = studyService.getDailyStudySet(userId, date)
        if (date != LocalDate.now()) {
            throw NotFoundException(message = "DailyStudySet not found", code = "STUDY_4")
        }

        if (dailyStudySet == null) {
            dailyStudySet = studyService.createDailyStudySet(DailyStudySetCreateObject(userId, date))
        }

        val endOfDay = now.atStartOfDay().plusDays(1).toInstant(ZoneOffset.UTC)
        val due = endOfDay.atZone(ZoneId.systemDefault()).toInstant()
        val studies =
            studyService.getStudiesByIds(dailyStudySet.studyIds) +
                studyService.getStudiesPastDue(
                    userId,
                    due,
                    DEFAULT_STUDY_SET_SIZE - dailyStudySet.studyIds.size,
                )
        val wordDefinitionIds = studies.map { it.wordDefinitionId }
        val words = wordService.getWordsByWordDefinitionIds(wordDefinitionIds)
        val wordByDefinitionIdMap = mutableMapOf<Long, Word>()
        words.forEach { word ->
            word.definitions.forEach { definition -> wordByDefinitionIdMap[definition.id] = word }
        }

        return Result(
            dailyStudySet = dailyStudySet.copy(studyIds = dailyStudySet.studyIds + studies.map { it.id }),
            studyWords =
                studies.map { study ->
                    StudyWord(
                        study = study,
                        word =
                            wordByDefinitionIdMap[study.wordDefinitionId]
                                ?: throw NotFoundException(message = "Data Integrity Error", code = "STUDY_2"),
                    )
                },
        )
    }
}

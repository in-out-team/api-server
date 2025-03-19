package com.inout.apiserver.application.study

import com.inout.apiserver.domain.study.DailyStudySetCreateObject
import com.inout.apiserver.domain.study.StudyService
import com.inout.apiserver.domain.study.StudyWord
import com.inout.apiserver.domain.word.WordService
import com.inout.apiserver.error.BadRequestException
import com.inout.apiserver.error.NotFoundException
import com.inout.apiserver.infrastructure.db.study.DailyStudySet
import com.inout.apiserver.infrastructure.db.user.User
import com.inout.apiserver.infrastructure.db.word.Word
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.ZoneId

@Component
class ReadOrCreateDailyStudySetApplication(
    private val studyService: StudyService,
    private val wordService: WordService,
) {
    data class Request(
        val user: User,
        val date: LocalDate,
    )

    data class Response(
        val dailyStudySet: DailyStudySet,
        val studyWords: List<StudyWord>,
    )

    fun run(request: Request): Response {
        val userZoneId = ZoneId.of(request.user.timezone)
        val now = LocalDate.now(userZoneId)
        if (request.date.isAfter(now)) {
            throw BadRequestException(message = "Cannot request future daily study set", code = "STUDY_3")
        }

        var dailyStudySet = studyService.getDailyStudySet(request.user.id!!, request.date)
        val isNotToday = request.date != now
        if (isNotToday && dailyStudySet == null) {
            throw NotFoundException(message = "DailyStudySet not found", code = "STUDY_4")
        }

        if (dailyStudySet == null) {
            dailyStudySet = studyService.createDailyStudySet(DailyStudySetCreateObject(request.user.id!!, request.date))
        }

        val studies = studyService.getStudiesByDailyStudySet(dailyStudySet, request.user)
        val wordDefinitionIds = studies.map { it.wordDefinitionId }
        val words = wordService.getWordsByWordDefinitionIds(wordDefinitionIds)
        val wordByDefinitionIdMap = mutableMapOf<Long, Word>()
        words.forEach { word ->
            word.definitions.forEach { definition -> wordByDefinitionIdMap[definition.id!!] = word }
        }

        return Response(
            dailyStudySet = dailyStudySet.copy(studyIds = studies.map { it.id!! }),
            studyWords =
                studies.map { study ->
                    StudyWord(
                        study = study,
                        word =
                            wordByDefinitionIdMap[study.wordDefinitionId]
                                ?.let { word ->
                                    word.copy(
                                        definitions =
                                            word.definitions
                                                .filter { definition ->
                                                    definition.id == study.wordDefinitionId
                                                }.also { definitions ->
                                                    if (definitions.isEmpty()) {
                                                        throw NotFoundException(
                                                            message = "Data Integrity Error",
                                                            code = "STUDY_2",
                                                        )
                                                    }
                                                },
                                    )
                                }
                                ?: throw NotFoundException(message = "Data Integrity Error", code = "STUDY_2"),
                    )
                },
        )
    }
}

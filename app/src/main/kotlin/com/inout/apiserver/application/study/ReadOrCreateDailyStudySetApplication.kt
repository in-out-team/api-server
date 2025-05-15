package com.inout.apiserver.application.study

import com.inout.apiserver.domain.study.MongoStudyService
import com.inout.apiserver.domain.study.MongoStudyWord
import com.inout.apiserver.domain.word.MongoWordService
import com.inout.apiserver.domain.word.WordWithDefinitions
import com.inout.apiserver.error.BadRequestException
import com.inout.apiserver.error.NotFoundException
import com.inout.apiserver.infrastructure.mongo.study.MongoDailyStudySet
import com.inout.apiserver.infrastructure.mongo.user.MongoUser
import org.bson.types.ObjectId
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.ZoneId

@Component
class ReadOrCreateDailyStudySetApplication(
    private val studyService: MongoStudyService,
    private val wordService: MongoWordService,
) {
    data class Request(
        val user: MongoUser,
        val date: LocalDate,
    )

    data class Response(
        val dailyStudySet: MongoDailyStudySet,
        val studyWords: List<MongoStudyWord>,
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
            dailyStudySet = studyService.createDailyStudySet(userId = request.user.id, date = request.date)
        }

        val studies = studyService.getStudiesByDailyStudySet(dailyStudySet, request.user)
        val wordDefinitionIds = studies.map { it.wordDefinitionId }
        val words = wordService.getWordsByLiveWordDefinitionIds(wordDefinitionIds)
        val wordByDefinitionIdMap = mutableMapOf<ObjectId, WordWithDefinitions>()
        words.forEach { word ->
            word.definitions.forEach { definition -> wordByDefinitionIdMap[definition.id!!] = word }
        }

        return Response(
            dailyStudySet = dailyStudySet.copy(studyIds = studies.map { it.id!! }),
            studyWords =
                studies.map { study ->
                    MongoStudyWord(
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
                                                }.toMutableList(),
                                    )
                                }
                                ?: throw NotFoundException(message = "Data Integrity Error", code = "STUDY_2"),
                    )
                },
        )
    }
}

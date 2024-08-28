package com.inout.apiserver.application.study

import com.inout.apiserver.domain.study.StudyService
import com.inout.apiserver.domain.study.StudyWord
import com.inout.apiserver.domain.word.Word
import com.inout.apiserver.domain.word.WordService
import com.inout.apiserver.error.InternalServerErrorException
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component

@Component
class ReadStudiesApplication(
    private val studyService: StudyService,
    private val wordService: WordService,
) {
    fun run(userId: Long, pageable: Pageable): Pair<Long, List<StudyWord>> {
        val studies = studyService.getAllByUserId(userId, pageable)
        val wordDefinitionIds = studies.content.map { it.wordDefinitionId }
        val words = wordService.getWordsByWordDefinitionIds(wordDefinitionIds)
        val wordByDefinitionIdMap = mutableMapOf<Long, Word>()
        words.forEach { word ->
            word.definitions.forEach { definition -> wordByDefinitionIdMap[definition.id] = word }
        }

        return Pair(
            studies.totalElements,
            studies.content.map { study ->
                StudyWord(
                    study = study,
                    word = wordByDefinitionIdMap[study.wordDefinitionId] ?: throw InternalServerErrorException(message = "Data Integrity Error", code = "STUDY_2"),
                )
            }
        )
    }
}

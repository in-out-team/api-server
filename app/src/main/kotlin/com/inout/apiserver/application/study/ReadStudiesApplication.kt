package com.inout.apiserver.application.study

import com.inout.apiserver.domain.study.StudyService
import com.inout.apiserver.domain.word.WordService
import com.inout.apiserver.interfaces.web.v1.response.StudyWithWordResponse
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component

@Component
class ReadStudiesApplication(
    private val studyService: StudyService,
    private val wordService: WordService,
) {
    /**
     * TODO:
     * application classes (use cases) should not return XXX_Response classes which reside in the interfaces layer
     * instead, they should return domain classes and the controller should convert them to response classes
     *
     * TODO:
     * should find a way to avoid N+1 queries
     */
    fun run(userId: Long, pageable: Pageable): Pair<Long, List<StudyWithWordResponse>> {
        val studies = studyService.getAllByUserId(userId, pageable)
        return Pair(
            studies.totalElements,
            studies.content.map { study ->
                StudyWithWordResponse.of(study, wordService.getWordByWordDefinitionId(study.wordDefinitionId))
            }
        )
    }
}

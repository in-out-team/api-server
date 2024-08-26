package com.inout.apiserver.application.study

import com.inout.apiserver.domain.study.StudyCreateObject
import com.inout.apiserver.domain.study.StudyService
import com.inout.apiserver.domain.study.StudyWord
import com.inout.apiserver.domain.word.WordService
import com.inout.apiserver.interfaces.web.v1.request.CreateStudyRequest
import com.inout.apiserver.interfaces.web.v1.response.StudyWordResponse
import org.springframework.stereotype.Component

@Component
class CreateStudyApplication(
    private val studyService: StudyService,
    private val wordService: WordService,
) {
    fun run(request: CreateStudyRequest, userId: Long): StudyWord {
        val studyCreateObject = StudyCreateObject(
            userId = userId,
            wordDefinitionId = request.wordDefinitionId,
        )
        val word = wordService.getWordByWordDefinitionId(request.wordDefinitionId)
        val createdStudy = studyService.createStudy(studyCreateObject)
        return StudyWord(study = createdStudy, word = word)
    }
}

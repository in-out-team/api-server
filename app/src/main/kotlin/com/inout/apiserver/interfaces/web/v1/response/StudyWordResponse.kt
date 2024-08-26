package com.inout.apiserver.interfaces.web.v1.response

import com.inout.apiserver.domain.study.Study
import com.inout.apiserver.domain.word.Word

data class StudyWordResponse(
    val id: Long,
    val word: WordWithDefinitionsResponse,
) {
    companion object {
        fun of(study: Study, word: Word): StudyWordResponse {
            val wordWithDefinitionsResponse = WordWithDefinitionsResponse.of(word)
            return StudyWordResponse(
                id = study.id,
                word = wordWithDefinitionsResponse.filterDefinitionsBy(study.wordDefinitionId)
            )
        }
    }
}

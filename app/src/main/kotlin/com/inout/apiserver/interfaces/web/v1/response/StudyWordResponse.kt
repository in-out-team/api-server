package com.inout.apiserver.interfaces.web.v1.response

import com.inout.apiserver.base.enums.FsrsCardRating
import com.inout.apiserver.infrastructure.db.study.Study
import com.inout.apiserver.infrastructure.db.word.Word

data class StudyWordResponse(
    val id: Long,
    val word: WordWithDefinitionsResponse,
    val lastRating: FsrsCardRating? = null,
) {
    companion object {
        fun of(
            study: Study,
            word: Word,
        ): StudyWordResponse {
            val wordWithDefinitionsResponse = WordWithDefinitionsResponse.of(word)
            return StudyWordResponse(
                id = study.id!!,
                word = wordWithDefinitionsResponse.filterDefinitionsBy(study.wordDefinitionId),
                lastRating = study.reviewLogs.lastOrNull()?.rating,
            )
        }
    }
}

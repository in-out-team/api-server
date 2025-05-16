package com.inout.apiserver.interfaces.web.v1.response

import com.inout.apiserver.base.enums.FsrsCardRating
import com.inout.apiserver.domain.word.WordWithDefinitions
import com.inout.apiserver.infrastructure.mongo.study.Study
import org.bson.types.ObjectId

data class StudyWordResponse(
    val id: ObjectId,
    val word: WordWithDefinitionsResponse,
    val lastRating: FsrsCardRating? = null,
) {
    companion object {
        fun of(
            study: Study,
            word: WordWithDefinitions,
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

package com.inout.apiserver.interfaces.web.v1.response

import com.inout.apiserver.base.enums.FsrsCardRating
import com.inout.apiserver.domain.word.WordWithDefinitions
import com.inout.apiserver.infrastructure.mongo.study.MongoStudy
import org.bson.types.ObjectId

data class MongoStudyWordResponse(
    val id: ObjectId,
    val word: MongoWordWithDefinitionsResponse,
    val lastRating: FsrsCardRating? = null,
) {
    companion object {
        fun of(
            study: MongoStudy,
            word: WordWithDefinitions,
        ): MongoStudyWordResponse {
            val wordWithDefinitionsResponse = MongoWordWithDefinitionsResponse.of(word)
            return MongoStudyWordResponse(
                id = study.id!!,
                word = wordWithDefinitionsResponse.filterDefinitionsBy(study.wordDefinitionId),
                lastRating = study.reviewLogs.lastOrNull()?.rating,
            )
        }
    }
}

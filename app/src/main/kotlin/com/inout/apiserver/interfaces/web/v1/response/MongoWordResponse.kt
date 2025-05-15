package com.inout.apiserver.interfaces.web.v1.response

import com.inout.apiserver.base.enums.LanguageType
import com.inout.apiserver.domain.word.WordWithDefinitions
import com.inout.apiserver.infrastructure.mongo.word.MongoWord
import org.bson.types.ObjectId

data class MongoWordResponse(
    val id: ObjectId,
    val name: String,
    val fromLanguage: LanguageType,
    val toLanguage: LanguageType,
) {
    companion object {
        fun of(word: MongoWord): MongoWordResponse =
            MongoWordResponse(
                id = word.id!!,
                name = word.name,
                fromLanguage = word.fromLanguage,
                toLanguage = word.toLanguage,
            )

        fun of(word: WordWithDefinitions): MongoWordResponse =
            MongoWordResponse(
                id = word.id!!,
                name = word.name,
                fromLanguage = word.fromLanguage,
                toLanguage = word.toLanguage,
            )
    }
}

package com.inout.apiserver.interfaces.web.v1.response

import com.inout.apiserver.base.enums.LanguageType
import com.inout.apiserver.domain.word.WordWithDefinitions
import org.bson.types.ObjectId

data class WordResponse(
    val id: ObjectId,
    val name: String,
    val fromLanguage: LanguageType,
    val toLanguage: LanguageType,
) {
    companion object {
        fun of(word: WordWithDefinitions): WordResponse =
            WordResponse(
                id = word.id!!,
                name = word.name,
                fromLanguage = word.fromLanguage,
                toLanguage = word.toLanguage,
            )
    }
}

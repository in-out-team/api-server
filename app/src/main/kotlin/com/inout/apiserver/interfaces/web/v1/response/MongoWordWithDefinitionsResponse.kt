package com.inout.apiserver.interfaces.web.v1.response

import com.inout.apiserver.base.enums.LanguageType
import com.inout.apiserver.domain.word.WordWithDefinitions
import org.bson.types.ObjectId

data class MongoWordWithDefinitionsResponse(
    val id: ObjectId,
    val name: String,
    val fromLanguage: LanguageType,
    val toLanguage: LanguageType,
    val definitions: List<MongoWordDefinitionResponse>,
) {
    companion object {
        fun of(word: WordWithDefinitions): MongoWordWithDefinitionsResponse =
            MongoWordWithDefinitionsResponse(
                id = word.id!!,
                name = word.name,
                fromLanguage = word.fromLanguage,
                toLanguage = word.toLanguage,
                definitions = word.definitions.map { MongoWordDefinitionResponse.of(it) },
            )
    }
}

package com.inout.apiserver.interfaces.web.v1.response

import com.inout.apiserver.base.enums.LanguageType
import com.inout.apiserver.domain.word.WordWithDefinitions
import org.bson.types.ObjectId

data class WordWithDefinitionsResponse(
    val id: ObjectId,
    val name: String,
    val fromLanguage: LanguageType,
    val toLanguage: LanguageType,
    val definitions: List<WordDefinitionResponse>,
) {
    companion object {
        fun of(word: WordWithDefinitions): WordWithDefinitionsResponse =
            WordWithDefinitionsResponse(
                id = word.id!!,
                name = word.name,
                fromLanguage = word.fromLanguage,
                toLanguage = word.toLanguage,
                definitions = word.definitions.map { WordDefinitionResponse.of(it) },
            )
    }

    fun filterDefinitionsBy(wordDefinitionId: ObjectId): WordWithDefinitionsResponse =
        copy(
            definitions =
                definitions.filter {
                    it.id == wordDefinitionId
                },
        )
}

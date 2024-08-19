package com.inout.apiserver.interfaces.web.v1.response

import com.inout.apiserver.base.enums.LanguageType
import com.inout.apiserver.base.enums.LexicalCategoryType
import com.inout.apiserver.domain.word.Word

data class WordWithDefinitionsResponse(
    val id: Long,
    val name: String,
    val fromLanguage: LanguageType,
    val toLanguage: LanguageType,
    val definitions: List<WordDefinitionResponse>
) {
    companion object {
        fun of(word: Word): WordWithDefinitionsResponse {
            return WordWithDefinitionsResponse(
                id = word.id,
                name = word.name,
                fromLanguage = word.fromLanguage,
                toLanguage = word.toLanguage,
                definitions = word.definitions.map { WordDefinitionResponse.of(it) }
            )
        }

        fun of(word: Word, lexicalCategoryType: LexicalCategoryType?): WordWithDefinitionsResponse {
            return WordWithDefinitionsResponse(
                id = word.id,
                name = word.name,
                fromLanguage = word.fromLanguage,
                toLanguage = word.toLanguage,
                definitions = word.definitions.filter {
                    lexicalCategoryType == null || it.lexicalCategory == lexicalCategoryType
                }.map {
                    WordDefinitionResponse.of(it)
                }
            )
        }
    }
}

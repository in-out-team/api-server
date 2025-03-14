package com.inout.apiserver.interfaces.web.v1.response

import com.inout.apiserver.base.enums.LanguageType
import com.inout.apiserver.base.enums.LexicalCategoryType
import com.inout.apiserver.infrastructure.db.word.Word

data class DictionaryWordWithDefinitionsResponse(
    val id: Long,
    val name: String,
    val fromLanguage: LanguageType,
    val toLanguage: LanguageType,
    val definitions: List<WordDefinitionWithStudyingResponse>,
) {
    companion object {
        fun of(
            word: Word,
            lexicalCategoryType: LexicalCategoryType?,
            studyingWordDefinitionIds: Set<Long>,
        ): DictionaryWordWithDefinitionsResponse =
            DictionaryWordWithDefinitionsResponse(
                id = word.id!!,
                name = word.name,
                fromLanguage = word.fromLanguage,
                toLanguage = word.toLanguage,
                definitions =
                    word.definitions
                        .filter {
                            lexicalCategoryType == null || it.lexicalCategory == lexicalCategoryType
                        }.map {
                            WordDefinitionWithStudyingResponse.of(it, it.id in studyingWordDefinitionIds)
                        },
            )
    }
}

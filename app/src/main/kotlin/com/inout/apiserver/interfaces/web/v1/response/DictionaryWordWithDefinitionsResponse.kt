package com.inout.apiserver.interfaces.web.v1.response

import com.inout.apiserver.base.enums.LanguageType
import com.inout.apiserver.base.enums.LexicalCategoryType
import com.inout.apiserver.domain.word.WordWithDefinitions
import org.bson.types.ObjectId

data class DictionaryWordWithDefinitionsResponse(
    val id: ObjectId,
    val name: String,
    val fromLanguage: LanguageType,
    val toLanguage: LanguageType,
    val definitions: List<WordDefinitionWithStudyingResponse>,
) {
    companion object {
        fun of(
            word: WordWithDefinitions,
            lexicalCategory: LexicalCategoryType?,
            studyingWordDefinitionIds: Set<ObjectId>,
        ): DictionaryWordWithDefinitionsResponse =
            DictionaryWordWithDefinitionsResponse(
                id = word.id!!,
                name = word.name,
                fromLanguage = word.fromLanguage,
                toLanguage = word.toLanguage,
                definitions =
                    word.definitions
                        .filter {
                            lexicalCategory == null || it.lexicalCategory == lexicalCategory
                        }.map {
                            WordDefinitionWithStudyingResponse.of(it, it.id in studyingWordDefinitionIds)
                        },
            )
    }
}

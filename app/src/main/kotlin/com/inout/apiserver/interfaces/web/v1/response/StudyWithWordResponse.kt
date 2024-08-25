package com.inout.apiserver.interfaces.web.v1.response

import com.inout.apiserver.base.enums.LanguageType
import com.inout.apiserver.base.enums.LexicalCategoryType
import com.inout.apiserver.domain.study.Study
import com.inout.apiserver.domain.word.Word

data class StudyWithWordResponse(
    val id: Long,
    val word: WordResponse,
    val wordDefinition: WordDefinitionResponse,
) {
    data class WordResponse(
        val id: Long,
        val name: String,
        val fromLanguage: LanguageType,
        val toLanguage: LanguageType,
    )

    data class WordDefinitionResponse(
        val id: Long,
        val lexicalCategory: LexicalCategoryType,
        val meaning: String,
        val preContext: String,
    )

    companion object {
        fun of(study: Study, word: Word): StudyWithWordResponse {
            val definition = word.definitions.first { it.id == study.wordDefinitionId }
            return StudyWithWordResponse(
                id = study.id,
                word = WordResponse(
                    id = word.id,
                    name = word.name,
                    fromLanguage = word.fromLanguage,
                    toLanguage = word.toLanguage,
                ),
                wordDefinition = WordDefinitionResponse(
                    id = definition.id,
                    lexicalCategory = definition.lexicalCategory,
                    meaning = definition.meaning,
                    preContext = definition.preContext,
                ),
            )
        }
    }
}

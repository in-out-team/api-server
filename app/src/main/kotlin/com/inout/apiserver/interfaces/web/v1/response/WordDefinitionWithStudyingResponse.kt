package com.inout.apiserver.interfaces.web.v1.response

import com.inout.apiserver.base.enums.LexicalCategoryType
import com.inout.apiserver.infrastructure.db.word.WordDefinition

data class WordDefinitionWithStudyingResponse(
    val id: Long,
    val lexicalCategory: LexicalCategoryType,
    val meaning: String,
    val preContext: String,
    val studying: Boolean,
) {
    companion object {
        fun of(
            wordDefinition: WordDefinition,
            studying: Boolean,
        ): WordDefinitionWithStudyingResponse =
            WordDefinitionWithStudyingResponse(
                id = wordDefinition.id!!,
                lexicalCategory = wordDefinition.lexicalCategory,
                meaning = wordDefinition.meaning,
                preContext = wordDefinition.preContext,
                studying = studying,
            )
    }
}

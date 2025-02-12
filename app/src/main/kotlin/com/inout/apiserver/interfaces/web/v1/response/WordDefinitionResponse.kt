package com.inout.apiserver.interfaces.web.v1.response

import com.inout.apiserver.base.enums.LexicalCategoryType
import com.inout.apiserver.infrastructure.db.word.WordDefinition

data class WordDefinitionResponse(
    val id: Long,
    val lexicalCategory: LexicalCategoryType,
    val meaning: String,
    val preContext: String,
) {
    companion object {
        fun of(wordDefinition: WordDefinition): WordDefinitionResponse =
            WordDefinitionResponse(
                id = wordDefinition.id!!,
                lexicalCategory = wordDefinition.lexicalCategory,
                meaning = wordDefinition.meaning,
                preContext = wordDefinition.preContext,
            )
    }
}

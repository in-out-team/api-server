package com.inout.apiserver.domain.word

import com.inout.apiserver.base.alias.WordDefinitionId
import com.inout.apiserver.base.enums.SentenceType
import com.inout.apiserver.infrastructure.db.word.Sentence

data class SentenceCreateObject(
    val wordDefinitionId: WordDefinitionId,
    val type: SentenceType,
    val content: String,
    val translation: String,
    val lexicalCategories: List<Sentence.LexicalCategoryInfo>,
) {
    init {
        require(wordDefinitionId > 0) { "wordDefinitionId must be greater than 0" }
        require(content.isNotBlank()) { "content must not be blank" }
        require(translation.isNotBlank()) { "translation must not be blank" }
        require(lexicalCategories.isNotEmpty()) { "lexicalCategories must not be empty" }
        // TODO: create sometimes fails because of this check, need to enhance ai prompt
        require(content.split(" ").size == lexicalCategories.size) { "content and lexicalCategories size must be equal" }
    }
}

package com.inout.apiserver.domain.word

import com.inout.apiserver.base.enums.SentenceType
import com.inout.apiserver.infrastructure.db.word.SentenceEntity

data class Sentence(
    val id: Long,
    val type: SentenceType,
    val wordDefinitionId: Long,
    val content: String,
    val translation: String,
    val lexicalCategories: List<SentenceEntity.LexicalCategoryInfo>,
)

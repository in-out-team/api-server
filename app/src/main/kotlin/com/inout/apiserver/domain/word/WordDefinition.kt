package com.inout.apiserver.domain.word

import com.inout.apiserver.base.enums.LexicalCategoryType

data class WordDefinition(
    val id: Long,
    val lexicalCategory: LexicalCategoryType,
    val meaning: String,
    val preContext: String,
)

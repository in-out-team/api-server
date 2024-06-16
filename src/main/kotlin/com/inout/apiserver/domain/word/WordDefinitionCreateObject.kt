package com.inout.apiserver.domain.word

import com.inout.apiserver.base.enums.LexicalCategoryType

data class WordDefinitionCreateObject(
    val lexicalCategory: LexicalCategoryType,
    val meaning: String,
    val preContext: String,
)

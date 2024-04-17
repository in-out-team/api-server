package com.inout.apiserver.domain.word

import com.inout.apiserver.infrastructure.db.word.LanguageTypes

data class WordCreateObject(
    val name: String,
    val language: LanguageTypes
)

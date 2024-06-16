package com.inout.apiserver.domain.word

import com.inout.apiserver.base.enums.LanguageType

data class WordCreateObject(
    val name: String,
    val language: LanguageType
)

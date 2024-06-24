package com.inout.apiserver.interfaces.web.v1.request

import com.inout.apiserver.base.enums.LanguageType

data class ReadWordRequest(
    val name: String,
    val fromLanguage: LanguageType,
    val toLanguage: LanguageType,
)

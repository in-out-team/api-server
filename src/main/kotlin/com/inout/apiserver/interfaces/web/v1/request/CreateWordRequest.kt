package com.inout.apiserver.interfaces.web.v1.request

import com.inout.apiserver.base.enums.LanguageType
import com.inout.apiserver.error.BadRequestException

data class CreateWordRequest(
    val name: String,
    val fromLanguage: LanguageType,
    val toLanguage: LanguageType
) {
    init {
        if (fromLanguage == toLanguage) {
            throw BadRequestException(message = "fromLanguage and toLanguage must be different", code = "WORD_CWR_1")
        }
    }
}

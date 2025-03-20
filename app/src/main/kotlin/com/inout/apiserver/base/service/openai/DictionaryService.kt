package com.inout.apiserver.base.service.openai

import com.inout.apiserver.base.enums.LanguageType
import com.inout.apiserver.base.service.openai.dto.Definition

interface DictionaryService {
    fun fetchWordDefinitions(
        word: String,
        fromLanguage: LanguageType,
        toLanguage: LanguageType,
    ): List<Definition>
}

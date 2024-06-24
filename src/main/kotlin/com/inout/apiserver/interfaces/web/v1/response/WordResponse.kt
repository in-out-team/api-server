package com.inout.apiserver.interfaces.web.v1.response

import com.inout.apiserver.domain.word.Word
import com.inout.apiserver.base.enums.LanguageType

data class WordResponse(
    val id: Long,
    val name: String,
    val fromLanguage: LanguageType,
    val toLanguage: LanguageType,
) {
    companion object {
        fun of(word: Word): WordResponse {
            return WordResponse(
                id = word.id,
                name = word.name,
                fromLanguage = word.fromLanguage,
                toLanguage = word.toLanguage,
            )
        }
    }
}

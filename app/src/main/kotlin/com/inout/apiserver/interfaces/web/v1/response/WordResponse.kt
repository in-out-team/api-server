package com.inout.apiserver.interfaces.web.v1.response

import com.inout.apiserver.base.enums.LanguageType
import com.inout.apiserver.infrastructure.db.word.Word

data class WordResponse(
    val id: Long,
    val name: String,
    val fromLanguage: LanguageType,
    val toLanguage: LanguageType,
) {
    companion object {
        fun of(word: Word): WordResponse =
            WordResponse(
                id = word.id!!,
                name = word.name,
                fromLanguage = word.fromLanguage,
                toLanguage = word.toLanguage,
            )
    }
}

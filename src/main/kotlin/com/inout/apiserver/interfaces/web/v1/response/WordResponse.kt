package com.inout.apiserver.interfaces.web.v1.response

import com.inout.apiserver.domain.word.Word
import com.inout.apiserver.infrastructure.db.word.LanguageTypes

data class WordResponse(
    val id: Long,
    val name: String,
    val language: LanguageTypes
) {
    companion object {
        fun of(word: Word): WordResponse {
            return WordResponse(
                id = word.id,
                name = word.name,
                language = word.language
            )
        }
    }
}

package com.inout.apiserver.domain.word

import com.inout.apiserver.base.enums.LanguageType
import java.time.Instant

data class Word(
    val id: Long,
    val name: String,
    val fromLanguage: LanguageType,
    val toLanguage: LanguageType,
    val definitions: List<WordDefinition>,
    val createdAt: Instant?,
    val updatedAt: Instant?,
)

package com.inout.apiserver.domain.word

import com.inout.apiserver.infrastructure.db.word.LanguageTypes
import java.time.LocalDateTime

data class Word(
    val id: Long,
    val name: String,
    val language: LanguageTypes,
    val createdAt: LocalDateTime?,
    val updatedAt: LocalDateTime?
)

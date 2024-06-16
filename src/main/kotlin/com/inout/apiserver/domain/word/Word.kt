package com.inout.apiserver.domain.word

import com.inout.apiserver.base.enums.LanguageType
import java.time.LocalDateTime

data class Word(
    val id: Long,
    val name: String,
    val language: LanguageType,
    val createdAt: LocalDateTime?,
    val updatedAt: LocalDateTime?
)

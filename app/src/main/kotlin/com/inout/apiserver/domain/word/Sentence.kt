package com.inout.apiserver.domain.word

data class Sentence(
    val id: Long,
    val wordDefinitionId: Long,
    val content: String,
    val translation: String,
)

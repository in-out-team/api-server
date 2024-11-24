package com.inout.apiserver.domain.word

data class SentenceCreateObject(
    val wordDefinitionId: Long,
    val content: String,
    val translation: String,
)

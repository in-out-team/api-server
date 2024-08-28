package com.inout.apiserver.domain.study

import java.time.LocalDateTime

data class Study(
    val id: Long,
    val userId: Long,
    // should be unmodifiable
    val wordDefinitionId: Long,
    val createdAt: LocalDateTime?,
    val updatedAt: LocalDateTime?,
)

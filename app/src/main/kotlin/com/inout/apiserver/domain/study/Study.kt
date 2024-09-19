package com.inout.apiserver.domain.study

import java.time.Instant

data class Study(
    val id: Long,
    val userId: Long,
    // should be unmodifiable
    val wordDefinitionId: Long,
    val createdAt: Instant?,
    val updatedAt: Instant?,
)

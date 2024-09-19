package com.inout.apiserver.domain.user

import java.time.Instant

data class User(
    val id: Long,
    val email: String,
    val password: String,
    val nickname: String,
    val createdAt: Instant?,
    val updatedAt: Instant?,
)

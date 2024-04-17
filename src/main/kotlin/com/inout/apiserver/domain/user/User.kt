package com.inout.apiserver.domain.user

import com.inout.apiserver.infrastructure.db.user.UserEntity
import java.time.LocalDateTime

data class User(
    val id: Long,
    val email: String,
    val password: String,
    val nickname: String,
    val createdAt: LocalDateTime?,
    val updatedAt: LocalDateTime?,
)

package com.inout.apiserver.domain.user

import com.inout.apiserver.infrastructure.db.user.UserEntity
import java.time.LocalDateTime

data class User(
    val id: Long?,
    val email: String,
    val password: String,
    val nickname: String,
    val createdAt: LocalDateTime?,
    val updatedAt: LocalDateTime?,
) {
    fun toEntity(): UserEntity {
        return UserEntity(
            id = id,
            email = email,
            password = password,
            nickname = nickname,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
    }

    companion object {
        fun newOf(email: String, password: String, nickname: String): User {
            return User(
                id = null,
                email = email.lowercase(),
                password = password,
                nickname = nickname,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now(),
            )
        }
    }
}

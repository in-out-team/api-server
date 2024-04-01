package com.inout.apiserver.domain

import com.inout.apiserver.entity.UserEntity

data class User(
    val id: Long?,
    val email: String,
    val password: String,
    val nickname: String,
) {
    fun toEntity(): UserEntity {
        return UserEntity(
            id = id,
            email = email,
            password = password,
            nickname = nickname,
        )
    }

    companion object {
        fun newOf(email: String, password: String, nickname: String): User {
            return User(
                id = null,
                email = email.lowercase(),
                password = password,
                nickname = nickname,
            )
        }
    }
}

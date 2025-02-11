package com.inout.apiserver.interfaces.web.v1.response

import com.inout.apiserver.infrastructure.db.user.User

data class UserResponse(
    val id: Long,
    val email: String,
    val nickname: String,
) {
    companion object {
        fun of(user: User): UserResponse =
            UserResponse(
                id = user.id!!,
                email = user.email,
                nickname = user.nickname,
            )
    }
}

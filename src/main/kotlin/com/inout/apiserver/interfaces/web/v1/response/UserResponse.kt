package com.inout.apiserver.interfaces.web.v1.response

import com.inout.apiserver.domain.user.User
import com.inout.apiserver.error.InOutRequireNotNullException

data class UserResponse(
    val id: Long,
    val email: String,
    val nickname: String
) {
    companion object {
        fun from(user: User): UserResponse {
            return UserResponse(
                id = user.id,
                email = user.email,
                nickname = user.nickname,
            )
        }
    }
}

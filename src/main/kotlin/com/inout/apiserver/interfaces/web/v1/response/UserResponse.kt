package com.inout.apiserver.interfaces.web.v1.response

import com.inout.apiserver.domain.User
import com.inout.apiserver.error.InOutRequireNotNullException

data class UserResponse(
    val id: Long,
    val email: String,
    val nickname: String
) {
    companion object {
        fun from(user: User): UserResponse {
            return UserResponse(
                id = user.id ?: throw InOutRequireNotNullException(message = "User id is null", code = "IORNN_USER_1"),
                email = user.email,
                nickname = user.nickname,
            )
        }
    }
}

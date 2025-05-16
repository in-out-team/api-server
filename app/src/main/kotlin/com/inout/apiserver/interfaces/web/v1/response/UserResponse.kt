package com.inout.apiserver.interfaces.web.v1.response

import com.inout.apiserver.infrastructure.mongo.user.User
import org.bson.types.ObjectId

data class UserResponse(
    val id: ObjectId,
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

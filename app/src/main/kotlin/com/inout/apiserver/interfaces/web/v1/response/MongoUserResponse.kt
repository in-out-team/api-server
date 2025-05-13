package com.inout.apiserver.interfaces.web.v1.response

import com.inout.apiserver.infrastructure.mongo.user.MongoUser
import org.bson.types.ObjectId

data class MongoUserResponse(
    val id: ObjectId,
    val email: String,
    val nickname: String,
) {
    companion object {
        fun of(user: MongoUser): MongoUserResponse =
            MongoUserResponse(
                id = user.id!!,
                email = user.email,
                nickname = user.nickname,
            )
    }
}

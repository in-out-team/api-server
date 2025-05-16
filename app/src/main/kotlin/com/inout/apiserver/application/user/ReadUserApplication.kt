package com.inout.apiserver.application.user

import com.inout.apiserver.domain.user.UserService
import com.inout.apiserver.error.NotFoundException
import com.inout.apiserver.infrastructure.mongo.user.User
import org.bson.types.ObjectId
import org.springframework.stereotype.Component

@Component
class ReadUserApplication(
    private val userService: UserService,
) {
    data class Request(
        val id: ObjectId,
    )

    data class Response(
        val user: User,
    )

    fun run(request: Request): Response =
        Response(
            user =
                userService.getUserById(request.id) ?: throw NotFoundException(
                    message = "User not found",
                    code = "USER_2",
                ),
        )
}

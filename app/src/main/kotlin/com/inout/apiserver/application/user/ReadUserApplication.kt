package com.inout.apiserver.application.user

import com.inout.apiserver.base.alias.UserId
import com.inout.apiserver.domain.user.UserService
import com.inout.apiserver.error.NotFoundException
import com.inout.apiserver.infrastructure.db.user.User
import org.springframework.stereotype.Component

@Component
class ReadUserApplication(
    private val userService: UserService,
) {
    data class Request(
        val id: UserId,
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

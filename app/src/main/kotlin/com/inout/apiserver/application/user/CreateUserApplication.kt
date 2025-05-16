package com.inout.apiserver.application.user

import com.inout.apiserver.domain.user.UserService
import com.inout.apiserver.infrastructure.mongo.user.User
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional
class CreateUserApplication(
    private val userService: UserService,
) {
    data class Request(
        val email: String,
        val password: String,
        val nickname: String,
    )

    data class Response(
        val newUser: User,
    )

    fun run(request: Request): Response {
        val newUser =
            userService.createUser(
                email = request.email,
                password = request.password,
                nickname = request.nickname,
            )
        return Response(newUser = newUser)
    }
}

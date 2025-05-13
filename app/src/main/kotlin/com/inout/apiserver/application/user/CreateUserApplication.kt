package com.inout.apiserver.application.user

import com.inout.apiserver.domain.user.MongoUserService
import com.inout.apiserver.infrastructure.mongo.user.MongoUser
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional
class CreateUserApplication(
    private val userService: MongoUserService,
) {
    data class Request(
        val email: String,
        val password: String,
        val nickname: String,
    )

    data class Response(
        val newUser: MongoUser,
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

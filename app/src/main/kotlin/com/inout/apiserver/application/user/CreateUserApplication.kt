package com.inout.apiserver.application.user

import com.inout.apiserver.base.constants.PROD_ENV
import com.inout.apiserver.domain.user.UserService
import com.inout.apiserver.error.BadRequestException
import com.inout.apiserver.infrastructure.mongo.user.User
import org.springframework.core.env.Environment
import org.springframework.core.env.Profiles
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional
class CreateUserApplication(
    private val userService: UserService,
    private val environment: Environment,
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
        if (environment.acceptsProfiles(Profiles.of(PROD_ENV))) {
            throw BadRequestException(
                message = "Cannot create user with plain email and password",
                code = "USER_2",
            )
        }

        val newUser =
            userService.createUser(
                email = request.email,
                password = request.password,
                nickname = request.nickname,
            )
        return Response(newUser = newUser)
    }
}

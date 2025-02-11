package com.inout.apiserver.application.user

import com.inout.apiserver.domain.user.UserService
import com.inout.apiserver.infrastructure.db.user.User
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional
class UpdateUserApplication(
    private val userService: UserService,
) {
    data class Request(
        val user: User,
        val newNickname: String,
    )

    data class Response(
        val updatedUser: User,
    )

    fun run(request: Request): Response {
        val updatedUser =
            userService.updateUser(
                user = request.user,
                nickname = request.newNickname,
            )
        return Response(updatedUser = updatedUser)
    }
}

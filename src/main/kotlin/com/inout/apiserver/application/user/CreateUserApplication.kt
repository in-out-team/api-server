package com.inout.apiserver.application.user

import com.inout.apiserver.controller.v1.request.CreateUserRequest
import com.inout.apiserver.controller.v1.response.UserResponse
import com.inout.apiserver.error.InOutRequireNotNullException
import com.inout.apiserver.service.UserService
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional
class CreateUserApplication(
    private val userService: UserService
) {
    fun run(request: CreateUserRequest): UserResponse {
        val newUser = userService.createUser(request)
        return UserResponse(
            id = newUser.id ?: throw InOutRequireNotNullException(message = "User id is null", code = "IORNN_USER_1"),
            email = newUser.email,
            nickname = newUser.nickname,
        )
    }
}
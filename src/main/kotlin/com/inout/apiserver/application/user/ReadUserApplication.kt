package com.inout.apiserver.application.user

import com.inout.apiserver.interfaces.web.v1.response.UserResponse
import com.inout.apiserver.error.NotFoundException
import com.inout.apiserver.service.UserService
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class ReadUserApplication(
    private val userService: UserService
) {
    // TODO: add authorization/user validation
    fun run(id: Long): UserResponse {
        val user = userService.getUserById(id) ?: throw NotFoundException(message = "User not found", code = "USER_2")
        return UserResponse.from(user)
    }
}
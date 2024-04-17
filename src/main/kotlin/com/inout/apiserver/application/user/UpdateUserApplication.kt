package com.inout.apiserver.application.user

import com.inout.apiserver.interfaces.web.v1.request.UpdateUserRequest
import com.inout.apiserver.interfaces.web.v1.response.UserResponse
import com.inout.apiserver.domain.user.UserService
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional
class UpdateUserApplication(
    private val userService: UserService
) {
    // TODO: add authorization/user validation
    fun run(request: UpdateUserRequest): UserResponse {
        val updatedUser = userService.updateUser(request)
        return UserResponse.from(updatedUser)
    }
}
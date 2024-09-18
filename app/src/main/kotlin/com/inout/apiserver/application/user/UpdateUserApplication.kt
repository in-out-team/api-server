package com.inout.apiserver.application.user

import com.inout.apiserver.domain.user.User
import com.inout.apiserver.interfaces.web.v1.request.UpdateUserRequest
import com.inout.apiserver.interfaces.web.v1.response.UserResponse
import com.inout.apiserver.domain.user.UserService
import com.inout.apiserver.error.ForbiddenException
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional
class UpdateUserApplication(
    private val userService: UserService,
) {
    fun run(
        request: UpdateUserRequest,
        requestUser: User,
    ): UserResponse {
        if (request.id != requestUser.id) {
            throw ForbiddenException(
                message = "Cannot update other user's information",
                code = "USER_3",
            )
        }

        val updatedUser = userService.updateUser(request)
        return UserResponse.of(updatedUser)
    }
}

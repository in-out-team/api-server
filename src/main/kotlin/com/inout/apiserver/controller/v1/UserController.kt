package com.inout.apiserver.controller.v1

import com.inout.apiserver.application.user.CreateUserApplication
import com.inout.apiserver.controller.v1.request.CreateUserRequest
import com.inout.apiserver.controller.v1.response.UserResponse
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/users")
class UserController(
    private val createUserApplication: CreateUserApplication
) {
    @PostMapping
    fun createUser(@RequestBody request: CreateUserRequest): UserResponse {
        return createUserApplication.run(request)
    }
}
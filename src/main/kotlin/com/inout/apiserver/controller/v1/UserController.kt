package com.inout.apiserver.controller.v1

import com.inout.apiserver.application.user.CreateUserApplication
import com.inout.apiserver.application.user.ReadUserApplication
import com.inout.apiserver.application.user.UpdateUserApplication
import com.inout.apiserver.controller.v1.request.CreateUserRequest
import com.inout.apiserver.controller.v1.request.UpdateUserRequest
import com.inout.apiserver.controller.v1.response.UserResponse
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/v1/users")
class UserController(
    private val createUserApplication: CreateUserApplication,
    private val updateUserApplication: UpdateUserApplication,
    private val readUserApplication: ReadUserApplication
) {
    @PostMapping
    fun createUser(@RequestBody request: CreateUserRequest): UserResponse {
        return createUserApplication.run(request)
    }

    @PutMapping
    fun updateUser(@RequestBody request: UpdateUserRequest): UserResponse {
        return updateUserApplication.run(request)
    }

    @GetMapping("/{id}")
    fun getUser(@PathVariable id: Long): UserResponse {
        return readUserApplication.run(id)
    }
}
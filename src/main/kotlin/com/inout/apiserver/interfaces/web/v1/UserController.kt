package com.inout.apiserver.interfaces.web.v1

import com.inout.apiserver.application.user.CreateUserApplication
import com.inout.apiserver.application.user.ReadUserApplication
import com.inout.apiserver.application.user.UpdateUserApplication
import com.inout.apiserver.interfaces.web.v1.apiSpec.UserApiSpec
import com.inout.apiserver.interfaces.web.v1.request.CreateUserRequest
import com.inout.apiserver.interfaces.web.v1.request.UpdateUserRequest
import com.inout.apiserver.interfaces.web.v1.response.UserResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.http.HttpStatus.*

@RestController
@RequestMapping("/v1/users")
class UserController(
    private val createUserApplication: CreateUserApplication,
    private val updateUserApplication: UpdateUserApplication,
    private val readUserApplication: ReadUserApplication
) : UserApiSpec {
    override fun createUser(@RequestBody request: CreateUserRequest): ResponseEntity<UserResponse> {
        return ResponseEntity(createUserApplication.run(request), CREATED)
    }

    override fun updateUser(@RequestBody request: UpdateUserRequest): ResponseEntity<UserResponse> {
        return ResponseEntity(updateUserApplication.run(request), OK)
    }

    @GetMapping("/{id}")
    fun getUser(@PathVariable id: Long): UserResponse {
        return readUserApplication.run(id)
    }
}
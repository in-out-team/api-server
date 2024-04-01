package com.inout.apiserver.controller.v1.request

data class CreateUserRequest(
    val email: String,
    val password: String,
    val nickname: String,
)

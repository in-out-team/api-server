package com.inout.apiserver.interfaces.web.v1.request

data class CreateUserRequest(
    val email: String,
    val password: String,
    val nickname: String,
)

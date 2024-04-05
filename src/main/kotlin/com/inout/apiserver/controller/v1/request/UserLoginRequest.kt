package com.inout.apiserver.controller.v1.request

data class UserLoginRequest(
    val email: String,
    val password: String
)

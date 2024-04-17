package com.inout.apiserver.interfaces.web.v1.request

data class UserLoginRequest(
    val email: String,
    val password: String
)

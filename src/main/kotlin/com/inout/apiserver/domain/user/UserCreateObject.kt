package com.inout.apiserver.domain.user

data class UserCreateObject(
    val email: String,
    val password: String,
    val nickname: String
)

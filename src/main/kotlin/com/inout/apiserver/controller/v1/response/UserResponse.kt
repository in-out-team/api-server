package com.inout.apiserver.controller.v1.response

data class UserResponse(
    val id: Long,
    val email: String,
    val nickname: String
)

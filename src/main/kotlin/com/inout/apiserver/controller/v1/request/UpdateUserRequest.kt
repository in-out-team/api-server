package com.inout.apiserver.controller.v1.request

data class UpdateUserRequest(
    val id: Long,
    val nickname: String
)

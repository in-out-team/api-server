package com.inout.apiserver.interfaces.web.v1.request

data class UpdateUserRequest(
    val id: Long,
    val nickname: String
)

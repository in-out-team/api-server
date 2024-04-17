package com.inout.apiserver.interfaces.web.v1.response

data class TokenResponse(
    val accessToken: String
    // TODO: add refresh token
)

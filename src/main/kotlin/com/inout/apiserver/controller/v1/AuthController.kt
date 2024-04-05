package com.inout.apiserver.controller.v1

import com.inout.apiserver.application.auth.AuthLoginApplication
import com.inout.apiserver.controller.v1.request.UserLoginRequest
import com.inout.apiserver.controller.v1.response.TokenResponse
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/auth")
class AuthController(
    private val authLoginApplication: AuthLoginApplication
) {
    @PostMapping("/login")
    fun login(@RequestBody request: UserLoginRequest): TokenResponse {
        return authLoginApplication.run(request)
    }

    // TODO: get refresh token
}
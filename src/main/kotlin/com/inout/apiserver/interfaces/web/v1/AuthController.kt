package com.inout.apiserver.interfaces.web.v1

import com.inout.apiserver.application.auth.AuthLoginApplication
import com.inout.apiserver.interfaces.web.v1.apiSpec.AuthApiSpec
import com.inout.apiserver.interfaces.web.v1.request.GoogleLoginRequest
import com.inout.apiserver.interfaces.web.v1.request.UserLoginRequest
import com.inout.apiserver.interfaces.web.v1.response.TokenResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/auth")
class AuthController(
    private val authLoginApplication: AuthLoginApplication
) : AuthApiSpec {
    override fun login(@RequestBody request: UserLoginRequest): ResponseEntity<TokenResponse> {
        return ResponseEntity.ok(authLoginApplication.run(request))
    }

    override fun googleLogin(@RequestBody request: GoogleLoginRequest): ResponseEntity<TokenResponse> {
        TODO()
    }

    // TODO: get refresh token
}

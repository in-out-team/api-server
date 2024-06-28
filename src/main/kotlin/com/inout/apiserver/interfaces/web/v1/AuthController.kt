package com.inout.apiserver.interfaces.web.v1

import com.inout.apiserver.application.auth.EmailPasswordLoginApplication
import com.inout.apiserver.application.auth.GoogleLoginApplication
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
    private val emailPasswordLoginApplication: EmailPasswordLoginApplication,
    private val googleLoginApplication: GoogleLoginApplication,
) : AuthApiSpec {
    override fun login(@RequestBody request: UserLoginRequest): ResponseEntity<TokenResponse> {
        return ResponseEntity.ok(emailPasswordLoginApplication.run(request))
    }

    override fun googleLogin(@RequestBody request: GoogleLoginRequest): ResponseEntity<TokenResponse> {
        return ResponseEntity.ok(googleLoginApplication.run(request))
    }

    // TODO: get refresh token
}

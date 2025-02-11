package com.inout.apiserver.interfaces.web.v1

import com.inout.apiserver.application.auth.EmailPasswordLoginApplication
import com.inout.apiserver.application.auth.GoogleLoginApplication
import com.inout.apiserver.interfaces.web.v1.apiSpec.AuthApiSpec
import com.inout.apiserver.interfaces.web.v1.request.GoogleLoginRequest
import com.inout.apiserver.interfaces.web.v1.request.UserLoginRequest
import com.inout.apiserver.interfaces.web.v1.response.TokenResponse
import jakarta.validation.Valid
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
    override fun login(
        @RequestBody @Valid request: UserLoginRequest,
    ): ResponseEntity<TokenResponse> =
        ResponseEntity.ok(
            TokenResponse(
                accessToken =
                    emailPasswordLoginApplication
                        .run(
                            EmailPasswordLoginApplication.Request(
                                email = request.email,
                                password = request.password,
                            ),
                        ).accessToken,
            ),
        )

    override fun googleLogin(
        @RequestBody @Valid request: GoogleLoginRequest,
    ): ResponseEntity<TokenResponse> =
        ResponseEntity.ok(
            TokenResponse(
                accessToken =
                    googleLoginApplication
                        .run(
                            GoogleLoginApplication.Request(idToken = request.idToken),
                        ).accessToken,
            ),
        )

    // TODO: get refresh token
}

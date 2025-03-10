package com.inout.apiserver.application.auth

import com.inout.apiserver.domain.auth.GoogleApiClientService
import com.inout.apiserver.domain.auth.TokenService
import com.inout.apiserver.domain.user.UserService
import org.apache.commons.codec.digest.Md5Crypt
import org.springframework.stereotype.Component

@Component
class GoogleLoginApplication(
    private val userService: UserService,
    private val googleApiClientService: GoogleApiClientService,
    private val tokenService: TokenService,
) {
    data class Request(
        val idToken: String,
    )

    data class Response(
        val accessToken: String,
        val refreshToken: String,
    )

    fun run(request: Request): Response {
        val email = googleApiClientService.extractEmail(request.idToken)
        val user =
            userService.getUserByEmail(email)
                ?: userService.createUser(
                    email = email,
                    // this password is not used for authentication
                    password = Md5Crypt.md5Crypt(email.toByteArray()),
                    nickname = email.split("@").first(),
                )
        val accessToken = tokenService.generateAccessToken(user, mapOf("userId" to user.id!!))
        val refreshToken = tokenService.generateRefreshToken(user, mapOf("userId" to user.id!!))

        return Response(accessToken = accessToken, refreshToken = refreshToken)
    }
}

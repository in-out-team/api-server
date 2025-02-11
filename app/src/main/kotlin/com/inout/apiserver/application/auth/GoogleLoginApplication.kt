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
    )

    fun run(request: Request): Response {
        val email = googleApiClientService.extractEmail(request.idToken)
        val user = userService.getUserByEmail(email)
        val accessToken =
            if (user != null) {
                tokenService.generate(user = user, extraClaims = mapOf("userId" to user.id!!))
            } else {
                val newUser =
                    userService.createUser(
                        email = email,
                        // this password is not used for authentication
                        password = Md5Crypt.md5Crypt(email.toByteArray()),
                        nickname = email.split("@").first(),
                    )
                tokenService.generate(newUser, extraClaims = mapOf("userId" to newUser.id!!))
            }

        return Response(accessToken = accessToken)
    }
}

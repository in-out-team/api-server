package com.inout.apiserver.application.auth

import com.inout.apiserver.config.jwt.JwtProperties
import com.inout.apiserver.interfaces.web.v1.request.UserLoginRequest
import com.inout.apiserver.interfaces.web.v1.response.TokenResponse
import com.inout.apiserver.error.InternalServerErrorException
import com.inout.apiserver.error.InvalidCredentialsException
import com.inout.apiserver.service.TokenService
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Component
import java.util.*

@Component
class AuthLoginApplication(
    private val tokenService: TokenService,
    private val authManager: AuthenticationManager,
    private val userDetailsService: UserDetailsService,
    private val jwtProperties: JwtProperties
) {
    fun run(request: UserLoginRequest): TokenResponse {
        validateRequest(request.email, request.password)
        val user = userDetailsService.loadUserByUsername(request.email)
        val now = System.currentTimeMillis()
        val accessToken = tokenService.generate(user, Date(now + jwtProperties.accessTokenExpiration))

        return TokenResponse(accessToken = accessToken)
    }

    private fun validateRequest(email: String, password: String) {
        runCatching {
            authManager.authenticate(UsernamePasswordAuthenticationToken(email, password))
        }.onFailure { action ->
            if (action is AuthenticationException) {
                throw InvalidCredentialsException(message = "Invalid credentials", code = "AUTH_1")
            }
            // TODO: add logging
            throw InternalServerErrorException(message = action.message ?: "Internal server error", code = "UNKNOWN_1")
        }
    }
}
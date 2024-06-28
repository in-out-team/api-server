package com.inout.apiserver.application.auth

import com.inout.apiserver.interfaces.web.v1.request.UserLoginRequest
import com.inout.apiserver.interfaces.web.v1.response.TokenResponse
import com.inout.apiserver.error.InternalServerErrorException
import com.inout.apiserver.error.InvalidCredentialsException
import com.inout.apiserver.domain.auth.TokenService
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Component

/**
 * TODO: change name to EmailPasswordLoginApplication
 */
@Component
class AuthLoginApplication(
    private val tokenService: TokenService,
    private val authManager: AuthenticationManager,
    private val userDetailsService: UserDetailsService,
) {
    fun run(request: UserLoginRequest): TokenResponse {
        validateRequest(request.email, request.password)
        /**
         * TODO:
         * - switch using authManager to userService
         */
        val user = userDetailsService.loadUserByUsername(request.email)
        // TODO: change to user method signature instead of userDetails
        val accessToken = tokenService.generate(userDetails = user)

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

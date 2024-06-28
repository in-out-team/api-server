package com.inout.apiserver.application.auth

import com.inout.apiserver.interfaces.web.v1.request.UserLoginRequest
import com.inout.apiserver.interfaces.web.v1.response.TokenResponse
import com.inout.apiserver.error.InternalServerErrorException
import com.inout.apiserver.error.InvalidCredentialsException
import com.inout.apiserver.domain.auth.TokenService
import com.inout.apiserver.domain.user.UserService
import com.inout.apiserver.error.NotFoundException
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.AuthenticationException
import org.springframework.stereotype.Component

@Component
class EmailPasswordLoginApplication(
    private val tokenService: TokenService,
    private val authManager: AuthenticationManager,
    private val userService: UserService,
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    fun run(request: UserLoginRequest): TokenResponse {
        validateRequest(request.email, request.password)
        val user = userService.getUserByEmail(request.email)
            ?: throw NotFoundException(message = "User not found", code = "USER_2")
        val accessToken = tokenService.generate(user)

        return TokenResponse(accessToken = accessToken)
    }

    private fun validateRequest(email: String, password: String) {
        runCatching {
            authManager.authenticate(UsernamePasswordAuthenticationToken(email, password))
        }.onFailure { exception ->
            if (exception is AuthenticationException) {
                throw InvalidCredentialsException(message = "Invalid credentials", code = "AUTH_1")
            }
            logger.error("Unexpected error occurred", exception)
            throw InternalServerErrorException(message = exception.message ?: "Internal server error", code = "UNKNOWN_1")
        }
    }
}

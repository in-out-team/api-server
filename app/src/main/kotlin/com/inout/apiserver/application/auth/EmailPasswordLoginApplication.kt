package com.inout.apiserver.application.auth

import com.inout.apiserver.domain.auth.MongoTokenService
import com.inout.apiserver.domain.user.MongoUserService
import com.inout.apiserver.error.InternalServerErrorException
import com.inout.apiserver.error.InvalidCredentialsException
import com.inout.apiserver.error.NotFoundException
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.AuthenticationException
import org.springframework.stereotype.Component

@Component
class EmailPasswordLoginApplication(
    private val tokenService: MongoTokenService,
    private val authManager: AuthenticationManager,
    private val userService: MongoUserService,
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    data class Request(
        val email: String,
        val password: String,
    )

    data class Response(
        val accessToken: String,
        val refreshToken: String,
    )

    fun run(request: Request): Response {
        validateRequest(request.email, request.password)
        val user =
            userService.getUserByEmail(request.email)
                ?: throw NotFoundException(message = "User not found", code = "USER_2")
        val accessToken = tokenService.generateAccessToken(user, mapOf("userId" to user.id!!.toString()))
        val refreshToken = tokenService.generateRefreshToken(user, mapOf("userId" to user.id.toString()))

        return Response(accessToken = accessToken, refreshToken = refreshToken)
    }

    private fun validateRequest(
        email: String,
        password: String,
    ) {
        runCatching {
            authManager.authenticate(UsernamePasswordAuthenticationToken(email, password))
        }.onFailure { exception ->
            if (exception is AuthenticationException) {
                throw InvalidCredentialsException(message = "Invalid credentials", code = "AUTH_1")
            }
            logger.error("Unexpected error occurred", exception)
            throw InternalServerErrorException(
                message = exception.message ?: "Internal server error",
                code = "UNKNOWN_1",
            )
        }
    }
}

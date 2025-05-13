package com.inout.apiserver.application.user

import com.inout.apiserver.domain.auth.TokenService
import com.inout.apiserver.domain.user.MongoUserService
import com.inout.apiserver.error.InvalidCredentialsException
import com.inout.apiserver.error.NotFoundException
import com.inout.apiserver.infrastructure.mongo.user.MongoUser
import com.inout.apiserver.infrastructure.mongo.user.RefreshToken
import org.bson.types.ObjectId
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class RefreshTokenApplication(
    private val userService: MongoUserService,
    private val tokenService: TokenService,
) {
    data class Request(
        val token: String,
    )

    data class Response(
        val accessToken: String,
        val refreshToken: String,
    )

    @Transactional
    fun run(request: Request): Response {
        val refreshToken = findRefreshTokenOrThrow(request.token)
        val user = findUserOrThrow(refreshToken.userId)
        validateTokenOrThrow(refreshToken, user.email)
        tokenService.deleteRefreshToken(refreshToken)

        return generateTokens(user)
    }

    private fun findRefreshTokenOrThrow(token: String) =
        tokenService.getByToken(token)
            ?: throw NotFoundException(code = "AUTH_2", message = "Refresh token not found")

    private fun findUserOrThrow(userId: ObjectId) =
        userService.getUserById(userId) ?: throw NotFoundException(code = "AUTH_3", message = "User not found")

    private fun validateTokenOrThrow(
        refreshToken: RefreshToken,
        email: String,
    ) {
        if (!tokenService.isValid(refreshToken.token, email)) {
            tokenService.deleteRefreshToken(refreshToken)
            throw InvalidCredentialsException(code = "AUTH_4", message = "Invalid refresh token")
        }
    }

    private fun generateTokens(user: MongoUser) =
        Response(
            accessToken = tokenService.generateAccessToken(user),
            refreshToken = tokenService.generateRefreshToken(user),
        )
}

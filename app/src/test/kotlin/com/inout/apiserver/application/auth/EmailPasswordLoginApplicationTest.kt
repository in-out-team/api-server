package com.inout.apiserver.application.auth

import com.inout.apiserver.config.jwt.JwtProperties
import com.inout.apiserver.interfaces.web.v1.request.UserLoginRequest
import com.inout.apiserver.error.InternalServerErrorException
import com.inout.apiserver.error.InvalidCredentialsException
import com.inout.apiserver.domain.auth.TokenService
import com.inout.apiserver.domain.user.User
import com.inout.apiserver.domain.user.UserService
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException

class EmailPasswordLoginApplicationTest {
    private val jwtProperties = JwtProperties(
        key = "super-long-secret-key-long-enough-to-have-a-size-over-256-bits",
        accessTokenExpiration = 1000L,
        refreshTokenExpiration = 1000L
    )
    private val tokenService = spyk(TokenService(jwtProperties))
    private val authManager = mockk<AuthenticationManager>()
    private val userService = mockk<UserService>()
    private val emailPasswordLoginApplication = EmailPasswordLoginApplication(tokenService, authManager, userService)

    @Test
    fun `run - should raise InvalidCredentialsException when invalid credentials`() {
        // given
        every { authManager.authenticate(any()) } throws BadCredentialsException("Invalid credentials")

        // when
        val exception = assertThrows(InvalidCredentialsException::class.java) {
            emailPasswordLoginApplication.run(UserLoginRequest("email@1.com", "password"))
        }

        // then
        assertEquals("Invalid credentials", exception.message)
        assertEquals("AUTH_1", exception.code)
    }

    @Test
    fun `run - should raise InternalServerErrorException when unexpected error occurs`() {
        // given
        every { authManager.authenticate(any()) } throws RuntimeException("Unexpected error")

        // when
        val exception = assertThrows(InternalServerErrorException::class.java) {
            emailPasswordLoginApplication.run(UserLoginRequest("email@1.com", "password"))
        }

        // then
        assertEquals("Unexpected error", exception.message)
        assertEquals("UNKNOWN_1", exception.code)
    }

    @Test
    fun `run - should return TokenResponse when user is authenticated successfully`() {
        // given
        val user = mockk<User>()
        every { userService.getUserByEmail("email@1.com") } returns user
        every { authManager.authenticate(any()) } returns null
        every { tokenService.generate(user, any(), any()) } returns "token"

        // when
        val result = emailPasswordLoginApplication.run(UserLoginRequest("email@1.com", "password"))

        // then
        assertEquals("token", result.accessToken)
        verify { userService.getUserByEmail("email@1.com") }
        verify { authManager.authenticate(any()) }
        verify { tokenService.generate(user, any(), any()) }
    }
}

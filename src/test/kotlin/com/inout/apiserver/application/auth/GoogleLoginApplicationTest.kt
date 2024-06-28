package com.inout.apiserver.application.auth

import com.inout.apiserver.config.jwt.JwtProperties
import com.inout.apiserver.domain.auth.GoogleApiClientService
import com.inout.apiserver.domain.auth.TokenService
import com.inout.apiserver.domain.user.User
import com.inout.apiserver.domain.user.UserService
import com.inout.apiserver.interfaces.web.v1.request.GoogleLoginRequest
import com.inout.apiserver.interfaces.web.v1.response.TokenResponse
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class GoogleLoginApplicationTest {
    private val userService = mockk<UserService>()
    private val googleApiClientService = mockk<GoogleApiClientService>()
    private val jwtProperties = JwtProperties(
        key = "super-long-secret-key-long-enough-to-have-a-size-over-256-bits",
        accessTokenExpiration = 1000L,
        refreshTokenExpiration = 1000L
    )
    private val tokenService = spyk(TokenService(jwtProperties))
    private val googleLoginApplication = GoogleLoginApplication(userService, googleApiClientService, tokenService)
    private val email = "test@1.com"

    @Test
    fun `run - should raise error if invalid idToken`() {
        // given
        val request = GoogleLoginRequest("invalid-id-token")
        every { googleApiClientService.extractEmail(request.idToken) } throws RuntimeException("Invalid idToken")

        // when
        val exception = assertThrows(RuntimeException::class.java) {
            googleLoginApplication.run(request)
        }

        // then
        assertEquals("Invalid idToken", exception.message)
    }

    @Test
    fun `run - should create user if user with email does not exist`() {
        // given
        val request = GoogleLoginRequest("valid-id-token")
        val newUser = mockk<User>()
        every { googleApiClientService.extractEmail(request.idToken) } returns email
        every { userService.getUserByEmail(email) } returns null
        every { userService.createUser(any()) } returns newUser
        every { tokenService.generate(newUser, any(), any()) } returns "accessToken"

        // when
        val sut: TokenResponse = googleLoginApplication.run(request)

        // then
        verify(exactly = 1) { userService.createUser(any()) }
        assertEquals("accessToken", sut.accessToken)
    }

    @Test
    fun `run - should not create user if user with email exists`() {
        // given
        val request = GoogleLoginRequest("valid-id-token")
        val user = mockk<User>()
        every { googleApiClientService.extractEmail(request.idToken) } returns email
        every { userService.getUserByEmail(email) } returns user
        every { tokenService.generate(user, any(), any()) } returns "accessToken"

        // when
        val sut: TokenResponse = googleLoginApplication.run(request)

        // then
        verify(exactly = 0) { userService.createUser(any()) }
        assertEquals("accessToken", sut.accessToken)
    }
}
package com.inout.apiserver.application.auth

import com.inout.apiserver.config.jwt.JwtProperties
import com.inout.apiserver.domain.auth.GoogleApiClientService
import com.inout.apiserver.domain.auth.TokenService
import com.inout.apiserver.domain.user.UserService
import com.inout.apiserver.infrastructure.db.user.User
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class GoogleLoginApplicationTest {
    private val userService = mockk<UserService>()
    private val googleApiClientService = mockk<GoogleApiClientService>()
    private val jwtProperties =
        JwtProperties(
            key = "super-long-secret-key-long-enough-to-have-a-size-over-256-bits",
            accessTokenExpiration = 1000L,
            refreshTokenExpiration = 1000L,
        )
    private val tokenService = spyk(TokenService(jwtProperties))
    private val googleLoginApplication = GoogleLoginApplication(userService, googleApiClientService, tokenService)
    private val email = "test@1.com"
    private val invalidIdToken = "invalid-id-token"
    private val validIdToken = "valid-id-token"

    @Test
    fun `run - should raise error if invalid idToken`() {
        // given
        every { googleApiClientService.extractEmail(invalidIdToken) } throws RuntimeException("Invalid idToken")

        // when
        val exception =
            assertThrows(RuntimeException::class.java) {
                googleLoginApplication.run(
                    GoogleLoginApplication.Request(idToken = invalidIdToken),
                )
            }

        // then
        assertEquals("Invalid idToken", exception.message)
    }

    @Test
    fun `run - should create user if user with email does not exist`() {
        // given
        val newUser =
            mockk<User> {
                every { id } returns 1L
            }
        every { googleApiClientService.extractEmail(validIdToken) } returns email
        every { userService.getUserByEmail(email) } returns null
        every { userService.createUser(any(), any(), any()) } returns newUser
        every { tokenService.generate(newUser, any(), any()) } returns "accessToken"

        // when
        val sut =
            googleLoginApplication.run(
                GoogleLoginApplication.Request(idToken = validIdToken),
            )

        // then
        verify(exactly = 1) { userService.createUser(any(), any(), any()) }
        assertEquals("accessToken", sut.accessToken)
    }

    @Test
    fun `run - should not create user if user with email exists`() {
        // given
        val user =
            mockk<User> {
                every { id } returns 1L
            }
        every { googleApiClientService.extractEmail(validIdToken) } returns email
        every { userService.getUserByEmail(email) } returns user
        every { tokenService.generate(user, any(), any()) } returns "accessToken"

        // when
        val sut =
            googleLoginApplication.run(
                GoogleLoginApplication.Request(idToken = validIdToken),
            )

        // then
        verify(exactly = 0) { userService.createUser(any(), any(), any()) }
        assertEquals("accessToken", sut.accessToken)
    }
}

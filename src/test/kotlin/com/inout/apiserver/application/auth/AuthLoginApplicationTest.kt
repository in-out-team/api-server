package com.inout.apiserver.application.auth

import com.inout.apiserver.config.jwt.JwtProperties
import com.inout.apiserver.controller.v1.request.UserLoginRequest
import com.inout.apiserver.error.InternalServerErrorException
import com.inout.apiserver.error.InvalidCredentialsException
import com.inout.apiserver.service.TokenService
import com.inout.apiserver.service.security.CustomUserDetailsService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService

class AuthLoginApplicationTest {
    private val tokenService = mockk<TokenService>()
    private val authManager = mockk<AuthenticationManager>()
    private val userDetailsService = mockk<CustomUserDetailsService>()
    private val jwtProperties = JwtProperties(
        key = "key",
        accessTokenExpiration = 1000L,
        refreshTokenExpiration = 1000L
    )
    private val authLoginApplication = AuthLoginApplication(tokenService, authManager, userDetailsService, jwtProperties)

    @Test
    fun `run - should raise InvalidCredentialsException when invalid credentials`() {
        // given
        every { authManager.authenticate(any()) } throws BadCredentialsException("Invalid credentials")

        // when
        val exception = assertThrows(InvalidCredentialsException::class.java) {
            authLoginApplication.run(UserLoginRequest("email@1.com", "password"))
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
            authLoginApplication.run(UserLoginRequest("email@1.com", "password"))
        }

        // then
        assertEquals("Unexpected error", exception.message)
        assertEquals("UNKNOWN_1", exception.code)
    }

    @Test
    fun `run - should return TokenResponse when user is authenticated successfully`() {
        // given
        val user = mockk<UserDetails>()
        every { userDetailsService.loadUserByUsername("email@1.com") } returns user
        every { authManager.authenticate(any()) } returns null
        every { tokenService.generate(user, any()) } returns "token"

        // when
        val result = authLoginApplication.run(UserLoginRequest("email@1.com", "password"))

        // then
        assertEquals("token", result.accessToken)
        verify { userDetailsService.loadUserByUsername("email@1.com") }
        verify { authManager.authenticate(any()) }
        verify { tokenService.generate(user, any()) }
    }
}
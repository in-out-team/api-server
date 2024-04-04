package com.inout.apiserver.service

import io.jsonwebtoken.Claims
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.core.userdetails.UserDetails
import java.lang.reflect.Method
import java.util.*

@SpringBootTest
class TokenServiceTest(
    @Autowired private val tokenService: TokenService,
) {
    private val userDetails = mockk<UserDetails>()

    @BeforeEach
    fun setUp() {
        every { userDetails.username } returns "example@1.com"
    }

    /**
     * since we are trying to test the feature of 3rd party library, use getClaims method existing in TokenService
     * which leverages the 3rd party library to get the claims from the token
     */
    private fun useTokenServiceGetClaimsWithToken(token: String): Claims {
        val getClaimsMethod: Method = TokenService::class.java
            .getDeclaredMethod("getClaims", String::class.java)
            .apply { isAccessible = true }
        return getClaimsMethod.invoke(tokenService, token) as Claims
    }

    @Test
    fun `generate - should generate a token with right claims`() {
        // given
        val expirationDate = Date(System.currentTimeMillis() + 1000L) // 1 second from now
        val extraClaims = mapOf("key1" to "value1", "key2" to "value2")

        // when
        val token = tokenService.generate(userDetails, expirationDate, extraClaims)

        // then
        assertTrue(token.isNotBlank())
        val claims = useTokenServiceGetClaimsWithToken(token)
        // check if expiration matches up to second
        // - this is because implementation of JwtBuilder's expiration method slices out milliseconds
        assertEquals(expirationDate.time / 1000, claims.expiration.time / 1000)
        assertEquals(userDetails.username, claims.subject)
        assertEquals(extraClaims["key1"], claims["key1"])
        assertEquals(extraClaims["key2"], claims["key2"])
    }

    @Test
    fun `isValid - should return true if token is valid and email matches`() {
        // given
        val expirationDate = Date(System.currentTimeMillis() + 1000L) // 1 second from now
        val token = tokenService.generate(userDetails, expirationDate)

        // when
        val isValid = tokenService.isValid(token, userDetails)

        // then
        assertTrue(isValid)
    }

    @Test
    fun `isValid - should return false if token is expired`() {
        // given
        val expirationDate = Date(System.currentTimeMillis() - 1000L) // 1 second ago
        val token = tokenService.generate(userDetails, expirationDate)

        // when & then
        assertFalse(tokenService.isValid(token, userDetails))
    }

    @Test
    fun `isValid - should return false if email does not match`() {
        // given
        val expirationDate = Date(System.currentTimeMillis() + 1000L) // 1 second from now
        val token = tokenService.generate(userDetails, expirationDate)
        val otherUserDetails = mockk<UserDetails>()
        every { otherUserDetails.username } returns userDetails.username + "1"

        // when & then
        assertFalse(tokenService.isValid(token, otherUserDetails))
    }

    @Test
    fun `isExpired - should return true if token is expired`() {
        // given
        val expirationDate = Date(System.currentTimeMillis() - 1000L) // 1 second ago
        val token = tokenService.generate(userDetails, expirationDate)

        // when
        val isExpired = tokenService.isExpired(token)

        // then
        assertTrue(isExpired)
    }

    @Test
    fun `isExpired - should return false if token is not expired`() {
        // given
        val expirationDate = Date(System.currentTimeMillis() + 1000L) // 1 second from now
        val token = tokenService.generate(userDetails, expirationDate)

        // when
        val isExpired = tokenService.isExpired(token)

        // then
        assertFalse(isExpired)
    }

    @Test
    fun `extractEmail - should return email if token is valid`() {
        // given
        val expirationDate = Date(System.currentTimeMillis() + 1000L) // 1 second from now
        val token = tokenService.generate(userDetails, expirationDate)

        // when
        val email = tokenService.extractEmail(token)

        // then
        assertEquals(userDetails.username, email)
    }

    @Test
    fun `extractEmail - should return null if token is invalid`() {
        // given
        val expirationDate = Date(System.currentTimeMillis() - 1000L) // 1 second ago
        val token = tokenService.generate(userDetails, expirationDate)

        // when
        val email = tokenService.extractEmail(token)

        // then
        assertNull(email)
    }
}
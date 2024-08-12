package com.inout.apiserver.domain.auth

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.inout.apiserver.error.GoogleIdTokenVerificationException
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class GoogleApiClientServiceTest {
    private val verifier = mockk<GoogleIdTokenVerifier>()
    private val googleApiClientService = GoogleApiClientService(verifier)
    private val invalidIdToken = "invalid.id.token"
    private val validIdToken = "valid.id.token"

    @Test
    fun `verifyIdToken - should throw InvalidCredentialsException when idToken is invalid`() {
        // given
        every { verifier.verify(invalidIdToken) } returns null

        // when
        val exception = assertThrows(GoogleIdTokenVerificationException::class.java) {
            googleApiClientService.verifyIdToken(invalidIdToken)
        }
        assertEquals("Invalid ID token", exception.message)
        assertEquals("GOOGLE_AUTH_1", exception.code)
    }

    @Test
    fun `verifyIdToken - should return GoogleIdToken when idToken is valid`() {
        // given
        val googleIdToken = mockk<GoogleIdToken>()
        every { verifier.verify(validIdToken) } returns googleIdToken

        // when
        val sut = googleApiClientService.verifyIdToken(validIdToken)

        // then
        assertTrue(sut is GoogleIdToken)
    }

    @Test
    fun `extractEmail - should return email from GoogleIdToken payload`() {
        // given
        val responseEmail = "test@1.com"
        val googleIdToken = mockk<GoogleIdToken> {
            every { payload } returns mockk {
                every { email } returns responseEmail
            }
        }
        every { verifier.verify(validIdToken) } returns googleIdToken

        // when
        val sut = googleApiClientService.extractEmail(validIdToken)

        // then
        assertEquals(responseEmail, sut)
    }
}

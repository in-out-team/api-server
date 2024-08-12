package com.inout.apiserver.domain.auth

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.inout.apiserver.error.GoogleIdTokenVerificationException
import org.springframework.stereotype.Service

@Service
class GoogleApiClientService(
    private val verifier: GoogleIdTokenVerifier,
) {
    fun extractEmail(idToken: String): String {
        val payload = verifyIdToken(idToken).payload
        return payload.email
    }

    fun verifyIdToken(idToken: String): GoogleIdToken {
        return verifier.verify(idToken)
            ?: throw GoogleIdTokenVerificationException(message = "Invalid ID token", code = "GOOGLE_AUTH_1")
    }
}

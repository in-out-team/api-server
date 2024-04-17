package com.inout.apiserver.domain.auth

import com.inout.apiserver.config.jwt.JwtProperties
import io.jsonwebtoken.Claims
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service
import java.util.*

@Service
class TokenService(
    jwtProperties: JwtProperties,
) {
    private val key = Keys.hmacShaKeyFor(jwtProperties.key.toByteArray())

    fun generate(
        userDetails: UserDetails,
        expirationDate: Date,
        extraClaims: Map<String, Any> = emptyMap()
    ): String {
        return Jwts.builder()
            .claims()
            .subject(userDetails.username) // userDetails.username is the email in this service.
            .issuedAt(Date(System.currentTimeMillis()))
            .expiration(expirationDate)
            .add(extraClaims)
            .and()
            .signWith(key)
            .compact()
    }

    fun isValid(token: String, userDetails: UserDetails): Boolean {
        return runCatching {
            extractEmail(token) == userDetails.username && !isExpired(token)
        }.getOrElse {
            false
        }
    }

    fun isExpired(token: String): Boolean {
        return runCatching {
            getClaims(token).expiration.before(Date(System.currentTimeMillis()))
        }.getOrElse { error ->
            when {
                error is ExpiredJwtException -> true
                else -> throw error
            }
        }
    }

    fun extractEmail(token: String): String? {
        return runCatching { getClaims(token).subject }.getOrElse { null }
    }

    /**
     * throws ExpiredJwtException if the token is expired or any other exception if the token is invalid
     */
    private fun getClaims(token: String): Claims {
        return Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload
    }
}
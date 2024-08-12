package com.inout.apiserver.domain.auth

import com.inout.apiserver.config.jwt.JwtProperties
import com.inout.apiserver.domain.user.User
import io.jsonwebtoken.Claims
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Service
import java.util.*

@Service
class TokenService(
    private val jwtProperties: JwtProperties,
) {
    private val key = Keys.hmacShaKeyFor(jwtProperties.key.toByteArray())

    fun generate(
        user: User,
        expirationDate: Date = Date(System.currentTimeMillis() + jwtProperties.accessTokenExpiration),
        extraClaims: Map<String, Any> = emptyMap()
    ): String {
        return Jwts.builder()
            .claims()
            .subject(user.email)
            .issuedAt(Date(System.currentTimeMillis()))
            .expiration(expirationDate)
            .add(extraClaims)
            .and()
            .signWith(key)
            .compact()
    }

    fun isValid(token: String, email: String): Boolean {
        return runCatching {
            extractEmail(token) == email && !isExpired(token)
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
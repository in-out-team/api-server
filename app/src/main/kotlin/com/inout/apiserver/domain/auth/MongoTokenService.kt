package com.inout.apiserver.domain.auth

import com.inout.apiserver.config.jwt.JwtProperties
import com.inout.apiserver.infrastructure.mongo.user.MongoRefreshToken
import com.inout.apiserver.infrastructure.mongo.user.MongoRefreshTokenRepository
import com.inout.apiserver.infrastructure.mongo.user.MongoUser
import io.jsonwebtoken.Claims
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Service
import java.util.Date

@Service
class MongoTokenService(
    private val jwtProperties: JwtProperties,
    private val mongoRefreshTokenRepository: MongoRefreshTokenRepository,
) {
    private val key = Keys.hmacShaKeyFor(jwtProperties.key.toByteArray())

    fun generate(
        user: MongoUser,
        expirationDate: Date = Date(System.currentTimeMillis() + jwtProperties.accessTokenExpiration),
        extraClaims: Map<String, Any> = emptyMap(),
    ): String =
        Jwts
            .builder()
            .claims()
            .subject(user.email)
            .issuedAt(Date(System.currentTimeMillis()))
            .expiration(expirationDate)
            .add(extraClaims)
            .and()
            .signWith(key)
            .compact()

    fun generateAccessToken(
        user: MongoUser,
        extraClaims: Map<String, Any> = emptyMap(),
    ): String {
        val expirationDate = Date(System.currentTimeMillis() + jwtProperties.accessTokenExpiration)
        return generate(user, expirationDate, extraClaims)
    }

    fun generateRefreshToken(
        user: MongoUser,
        extraClaims: Map<String, Any> = emptyMap(),
    ): String {
        val expirationDate = Date(System.currentTimeMillis() + jwtProperties.refreshTokenExpiration)
        val refreshToken =
            mongoRefreshTokenRepository.save(
                MongoRefreshToken(
                    userId = user.id!!,
                    token = generate(user, expirationDate, extraClaims),
                    expiresAt = expirationDate.toInstant(),
                ),
            )
        return refreshToken.token
    }

    fun getByToken(token: String): MongoRefreshToken? = mongoRefreshTokenRepository.findByToken(token)

    fun deleteRefreshToken(mongoRefreshToken: MongoRefreshToken) = mongoRefreshTokenRepository.delete(mongoRefreshToken)

    fun isValid(
        token: String,
        email: String,
    ): Boolean =
        runCatching {
            extractEmail(token) == email && !isExpired(token)
        }.getOrElse {
            false
        }

    fun isExpired(token: String): Boolean =
        runCatching {
            getClaims(token).expiration.before(Date(System.currentTimeMillis()))
        }.getOrElse { error ->
            when {
                error is ExpiredJwtException -> true
                else -> throw error
            }
        }

    fun extractEmail(token: String): String? = runCatching { getClaims(token).subject }.getOrElse { null }

    /**
     * throws ExpiredJwtException if the token is expired or any other exception if the token is invalid
     */
    fun getClaims(token: String): Claims =
        Jwts
            .parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload
}

package com.inout.apiserver.domain.auth

import com.inout.apiserver.base.helper.ClockProvider
import com.inout.apiserver.domain.user.UserFactory
import com.inout.apiserver.extension.cleanUp
import com.inout.apiserver.helper.InOutSpringBootTest
import com.inout.apiserver.infrastructure.db.user.RefreshToken
import com.inout.apiserver.infrastructure.db.user.RefreshTokenRepository
import com.inout.apiserver.infrastructure.db.user.User
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.mockito.kotlin.any
import org.mockito.kotlin.clearInvocations
import org.mockito.kotlin.eq
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.jdbc.core.JdbcTemplate
import java.util.Date

@InOutSpringBootTest
class TokenServiceTest(
    @SpyBean
    private val subject: TokenService,
    // repositories
    private val refreshTokenRepository: RefreshTokenRepository,
    // factories
    private val userFactory: UserFactory,
    // ect
    private val jdbcTemplate: JdbcTemplate,
) : DescribeSpec({
        var user: User? = null

        beforeEach {
            user = userFactory.createUser()
        }

        afterEach {
            jdbcTemplate.cleanUp()
            clearInvocations(subject)
        }

        describe("generate") {
            it("should create a token with given user, expiration date and extraClaims") {
                // given
                val expirationDate = Date(System.currentTimeMillis() + 1000L) // 1 second from now
                val extraClaims = mapOf("key1" to "value1", "key2" to "value2")

                // when
                val token = subject.generate(user!!, expirationDate, extraClaims)

                // then
                token.isNotEmpty() shouldBe true
                val claims = subject.getClaims(token)
                (claims.expiration.time - expirationDate.time) / 1000 shouldBe 0
                claims.subject shouldBe user!!.email
                claims["key1"] shouldBe extraClaims["key1"]
                claims["key2"] shouldBe extraClaims["key2"]
            }
        }

        describe("generateAccessToken") {
            it("should internally call generate with accessTokenExpiration") {
                // given
                val extraClaims = mapOf("key1" to "value1", "key2" to "value2")

                // when
                val token = subject.generateAccessToken(user!!, extraClaims)

                // then
                token.isNotEmpty() shouldBe true
                val claims = subject.getClaims(token)
                claims.subject shouldBe user!!.email
                claims["key1"] shouldBe extraClaims["key1"]
                claims["key2"] shouldBe extraClaims["key2"]
                verify(subject, times(1)).generate(eq(user!!), any(), eq(extraClaims))
            }
        }

        describe("generateRefreshToken") {
            it("should internally call generate with refreshTokenExpiration") {
                // given
                val extraClaims = mapOf("key1" to "value1", "key2" to "value2")

                // when
                val token = subject.generateRefreshToken(user!!, extraClaims)

                // then
                token.isNotEmpty() shouldBe true
                val claims = subject.getClaims(token)
                claims.subject shouldBe user!!.email
                claims["key1"] shouldBe extraClaims["key1"]
                claims["key2"] shouldBe extraClaims["key2"]
                verify(subject, times(1)).generate(eq(user!!), any(), eq(extraClaims))
            }

            it("should save the token to the database") {
                // given
                refreshTokenRepository.count() shouldBe 0
                val extraClaims = mapOf("key1" to "value1", "key2" to "value2")

                // when
                val token = subject.generateRefreshToken(user!!, extraClaims)

                // then
                val refreshToken = refreshTokenRepository.findByToken(token)
                refreshToken shouldNotBe null
                refreshToken!!.userId shouldBe user!!.id
                refreshToken.token shouldBe token
                refreshToken.expiresAt shouldBeGreaterThan ClockProvider.now()
            }
        }

        describe("getByToken") {
            it("should return the refresh token by token") {
                // given
                val refreshToken =
                    refreshTokenRepository.save(
                        RefreshToken(
                            userId = user!!.id!!,
                            token = "token",
                            expiresAt = ClockProvider.now(),
                        ),
                    )

                // when
                val result = subject.getByToken("token")

                // then
                result shouldBe refreshToken
            }
        }

        describe("deleteRefreshToken") {
            it("should delete the refresh token") {
                // given
                val refreshToken =
                    refreshTokenRepository.save(
                        RefreshToken(
                            userId = user!!.id!!,
                            token = "token",
                            expiresAt = ClockProvider.now(),
                        ),
                    )

                // when
                subject.deleteRefreshToken(refreshToken)

                // then
                refreshTokenRepository.findByToken("token") shouldBe null
            }
        }

        describe("isValid") {
            it("should return true if token is valid and email matches") {
                // given
                val expirationDate = Date(System.currentTimeMillis() + 1000L) // 1 second from now
                val token = subject.generate(user!!, expirationDate)

                // when
                val isValid = subject.isValid(token, user!!.email)

                // then
                isValid shouldBe true
            }

            it("should return false if token is expired") {
                // given
                val expirationDate = Date(System.currentTimeMillis() - 1000L) // 1 second ago
                val token = subject.generate(user!!, expirationDate)

                // when & then
                subject.isValid(token, user!!.email) shouldBe false
            }

            it("should return false if email does not match") {
                // given
                val expirationDate = Date(System.currentTimeMillis() + 1000L) // 1 second from now
                val token = subject.generate(user!!, expirationDate)

                // when & then
                subject.isValid(token, user!!.email + "1") shouldBe false
            }
        }

        describe("isExpired") {
            it("should return true if token is expired") {
                // given
                val expirationDate = Date(System.currentTimeMillis() - 1000L) // 1 second ago
                val token = subject.generate(user!!, expirationDate)

                // when
                val isExpired = subject.isExpired(token)

                // then
                isExpired shouldBe true
            }

            it("should return false if token is not expired") {
                // given
                val expirationDate = Date(System.currentTimeMillis() + 1000L) // 1 second from now
                val token = subject.generate(user!!, expirationDate)

                // when
                val isExpired = subject.isExpired(token)

                // then
                isExpired shouldBe false
            }
        }

        describe("extractEmail") {
            it("should return email if token is valid") {
                // given
                val expirationDate = Date(System.currentTimeMillis() + 1000L) // 1 second from now
                val token = subject.generate(user!!, expirationDate)

                // when
                val email = subject.extractEmail(token)

                // then
                email shouldBe user!!.email
            }

            it("should return null if token is invalid") {
                // given
                val expirationDate = Date(System.currentTimeMillis() - 1000L) // 1 second ago
                val token = subject.generate(user!!, expirationDate)

                // when
                val email = subject.extractEmail(token)

                // then
                email shouldBe null
            }
        }

        describe("getClaims") {
            it("should return claims from the token") {
                // given
                val expirationDate = Date(System.currentTimeMillis() + 1000L) // 1 second from now
                val token = subject.generate(user!!, expirationDate)

                // when
                val claims = subject.getClaims(token)

                // then
                claims.subject shouldBe user!!.email
            }
        }
    })

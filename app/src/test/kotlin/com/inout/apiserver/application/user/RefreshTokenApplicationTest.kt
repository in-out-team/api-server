package com.inout.apiserver.application.user

import com.inout.apiserver.domain.auth.TokenService
import com.inout.apiserver.domain.user.UserFactory
import com.inout.apiserver.error.InvalidCredentialsException
import com.inout.apiserver.error.NotFoundException
import com.inout.apiserver.extension.cleanUp
import com.inout.apiserver.helper.InOutSpringBootTest
import com.inout.apiserver.infrastructure.db.user.RefreshToken
import com.inout.apiserver.infrastructure.db.user.RefreshTokenRepository
import com.inout.apiserver.infrastructure.db.user.User
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.springframework.jdbc.core.JdbcTemplate
import java.util.Date

@InOutSpringBootTest
class RefreshTokenApplicationTest(
    private val subject: RefreshTokenApplication,
    // services
    private val tokenService: TokenService,
    // repositories
    private val refreshTokenRepository: RefreshTokenRepository,
    // factories
    private val userFactory: UserFactory,
    // etc
    private val jdbcTemplate: JdbcTemplate,
) : DescribeSpec({
        var user: User? = null
        var refreshToken: RefreshToken? = null

        beforeEach {
            user = userFactory.createUser()
        }

        afterEach {
            jdbcTemplate.cleanUp()
        }

        describe("when given token does not exist") {
            it("should throw NotFoundException") {
                // given
                val request = RefreshTokenApplication.Request("invalid-token")

                // when
                val exception =
                    shouldThrow<NotFoundException> {
                        subject.run(request)
                    }

                // then
                exception.code shouldBe "AUTH_2"
                exception.message shouldBe "Refresh token not found"
            }
        }

        describe("when user of given token does not exist") {
            it("should throw NotFoundException") {
                // given
                val nonExistingUser =
                    User(
                        id = Long.MAX_VALUE,
                        email = "nonexistingemail@1.com",
                        password = "password",
                        nickname = "nickname",
                    )
                val expirationDate = Date(System.currentTimeMillis() + 1000)
                val nonExistingUserToken =
                    tokenService.generate(
                        user = nonExistingUser,
                        expirationDate = expirationDate,
                        extraClaims = mapOf("userId" to nonExistingUser.id!!),
                    )
                refreshToken =
                    refreshTokenRepository.save(
                        RefreshToken(
                            userId = nonExistingUser.id!!,
                            token = nonExistingUserToken,
                            expiresAt = expirationDate.toInstant(),
                        ),
                    )
                val request = RefreshTokenApplication.Request(refreshToken!!.token)

                // when
                val exception =
                    shouldThrow<NotFoundException> {
                        subject.run(request)
                    }

                // then
                exception.code shouldBe "AUTH_3"
                exception.message shouldBe "User not found"
            }
        }

        describe("when given token is invalid") {
            it("should throw InvalidCredentialsException") {
                // given
                val expirationDate = Date(System.currentTimeMillis())
                val invalidToken =
                    tokenService.generate(
                        user = user!!,
                        expirationDate = expirationDate,
                        extraClaims = mapOf("userId" to user!!.id!!),
                    )
                refreshToken =
                    refreshTokenRepository.save(
                        RefreshToken(
                            userId = user!!.id!!,
                            token = invalidToken,
                            expiresAt = expirationDate.toInstant(),
                        ),
                    )
                val request = RefreshTokenApplication.Request(refreshToken!!.token)

                // when
                val exception =
                    shouldThrow<InvalidCredentialsException> {
                        subject.run(request)
                    }

                // then
                exception.code shouldBe "AUTH_4"
                exception.message shouldBe "Invalid refresh token"
            }
        }

        describe("when given token is valid") {
            it("should return new access token and refresh token") {
                // given
                val expirationDate = Date(System.currentTimeMillis() + 1000)
                val validToken =
                    tokenService.generate(
                        user = user!!,
                        expirationDate = expirationDate,
                        extraClaims = mapOf("userId" to user!!.id!!),
                    )
                refreshToken =
                    refreshTokenRepository.save(
                        RefreshToken(
                            userId = user!!.id!!,
                            token = validToken,
                            expiresAt = expirationDate.toInstant(),
                        ),
                    )
                val request = RefreshTokenApplication.Request(refreshToken!!.token)

                // when
                val response = subject.run(request)

                // then
                response.accessToken.isNotEmpty() shouldBe true
                response.refreshToken.isNotEmpty() shouldBe true
            }
        }
    })

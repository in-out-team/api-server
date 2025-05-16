package com.inout.apiserver.application.auth

import com.inout.apiserver.domain.auth.GoogleApiClientService
import com.inout.apiserver.domain.auth.TokenService
import com.inout.apiserver.domain.user.UserFactory
import com.inout.apiserver.domain.user.UserService
import com.inout.apiserver.error.GoogleIdTokenVerificationException
import com.inout.apiserver.extension.cleanUp
import com.inout.apiserver.helper.InOutSpringBootTest
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.whenever
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.data.mongodb.core.MongoTemplate

@InOutSpringBootTest
class GoogleLoginApplicationTest(
    private val subject: GoogleLoginApplication,
    // services
    private val userService: UserService,
    @SpyBean
    private val tokenService: TokenService,
    @SpyBean
    private val googleApiClientService: GoogleApiClientService,
    // factories
    private val userFactory: UserFactory,
    // etc
    private val mongoTemplate: MongoTemplate,
) : DescribeSpec({
        val email = "test@1.com"
        var idToken = ""

        beforeEach {
            doReturn(email)
                .whenever(googleApiClientService)
                .extractEmail("valid-id-token")

            doThrow(GoogleIdTokenVerificationException(message = "Invalid ID token", code = "GOOGLE_AUTH_1"))
                .whenever(googleApiClientService)
                .extractEmail("invalid-id-token")
        }

        afterEach {
            mongoTemplate.cleanUp()
        }

        describe("when provided with an invalid idToken") {
            beforeEach {
                idToken = "invalid-id-token"
            }

            it("should raise GoogleIdTokenVerificationException") {
                // when
                val exception =
                    shouldThrow<GoogleIdTokenVerificationException> {
                        subject.run(GoogleLoginApplication.Request(idToken = idToken))
                    }

                // then
                exception.message shouldBe "Invalid ID token"
                exception.code shouldBe "GOOGLE_AUTH_1"
            }
        }

        describe("when provided with a valid idToken") {
            beforeEach {
                idToken = "valid-id-token"
            }

            describe("when user with email does not exist") {
                it("should create user") {
                    // given
                    userService.getUserByEmail(email) shouldBe null

                    // when
                    val result = subject.run(GoogleLoginApplication.Request(idToken = idToken))

                    // then
                    result.accessToken.isNotEmpty() shouldBe true
                    result.refreshToken.isNotEmpty() shouldBe true
                    userService.getUserByEmail(email) shouldNotBe null
                }
            }

            describe("when user with email exists") {
                it("should not create a new user and return tokens") {
                    // given
                    val user = userFactory.createUser(email = email)
                    userService.getUserByEmail(user.email) shouldNotBe null

                    // when
                    val result = subject.run(GoogleLoginApplication.Request(idToken = idToken))

                    // then
                    result.accessToken.isNotEmpty() shouldBe true
                    result.refreshToken.isNotEmpty() shouldBe true
                    tokenService.extractEmail(result.accessToken) shouldBe email
                    tokenService.extractEmail(result.refreshToken) shouldBe email
                    tokenService.isValid(result.accessToken, user.email) shouldBe true
                    tokenService.isValid(result.refreshToken, user.email) shouldBe true
                }
            }
        }
    })

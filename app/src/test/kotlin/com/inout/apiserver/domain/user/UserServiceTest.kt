package com.inout.apiserver.domain.user

import com.inout.apiserver.base.enums.LanguageType
import com.inout.apiserver.error.ConflictException
import com.inout.apiserver.extension.cleanUp
import com.inout.apiserver.helper.InOutSpringBootTest
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.bson.types.ObjectId
import org.junit.jupiter.api.assertThrows
import org.springframework.data.mongodb.core.MongoTemplate

@InOutSpringBootTest
class UserServiceTest(
    private val subject: MongoUserService,
    // factories
    private val userFactory: MongoUserFactory,
    // etc
    private val mongoTemplate: MongoTemplate,
) : DescribeSpec({
        afterEach {
            mongoTemplate.cleanUp()
        }

        describe("createUser") {
            it("should throw error when user already exists") {
                // Given
                val existingUser = userFactory.createUser()
                val email = existingUser.email
                val password = "password"
                val nickname = "nickname"

                // When
                val exception =
                    assertThrows<ConflictException> {
                        subject.createUser(email = email, password = password, nickname = nickname)
                    }

                // Then
                exception.message shouldBe "User already exists"
                exception.code shouldBe "USER_1"
            }

            it("should save user when user does not exist") {
                // Given
                val email = "test@1.com"
                val password = "password"
                val nickname = "nickname"

                // When
                val result = subject.createUser(email = email, password = password, nickname = nickname)

                // Then
                result.id shouldNotBe null
                result.email shouldBe email
                result.nickname shouldBe nickname
                result.nativeLanguage shouldBe LanguageType.KOREAN
                result.studyLanguage shouldBe LanguageType.ENGLISH
                result.studyPerDay shouldBe 5
                result.timezone shouldBe "Asia/Seoul"
            }
        }

        describe("updateUser") {
            it("should update user") {
                // Given
                val user = userFactory.createUser()
                val newNickname = "newNickname"
                val newStudyLanguage = LanguageType.KOREAN
                val newNativeLanguage = LanguageType.ENGLISH
                val newStudyPerDay = 10
                val newTimezone = "Asia/New_York"

                // When
                val result =
                    subject.updateUser(
                        user = user,
                        nickname = newNickname,
                        studyLanguage = newStudyLanguage,
                        nativeLanguage = newNativeLanguage,
                        studyPerDay = newStudyPerDay,
                        timezone = newTimezone,
                    )

                // Then
                result.id shouldBe user.id
                result.email shouldBe user.email
                result.password shouldBe user.password
                result.nickname shouldBe newNickname
                result.studyLanguage shouldBe newStudyLanguage
                result.nativeLanguage shouldBe newNativeLanguage
                result.studyPerDay shouldBe newStudyPerDay
                result.timezone shouldBe newTimezone
            }
        }

        describe("getUserByEmail") {
            it("should return user") {
                // Given
                val user = userFactory.createUser()

                // When
                val result = subject.getUserByEmail(user.email)

                // Then
                result shouldBe user
            }

            it("should return null when user does not exist") {
                // Given
                val email = "test@1.com"

                // When
                val result = subject.getUserByEmail(email)

                // Then
                result shouldBe null
            }
        }

        describe("getUserById") {
            it("should return user") {
                // Given
                val user = userFactory.createUser()

                // When
                val result = subject.getUserById(user.id!!)

                // Then
                result shouldBe user
            }

            it("should return null when user does not exist") {
                // Given
                val id = ObjectId()

                // When
                val result = subject.getUserById(id)

                // Then
                result shouldBe null
            }
        }
    })

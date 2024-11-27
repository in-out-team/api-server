package com.inout.apiserver.domain.word

import com.inout.apiserver.error.ConflictException
import com.inout.apiserver.extension.cleanUp
import com.inout.apiserver.helper.InOutSpringBootTest
import com.inout.apiserver.infrastructure.db.word.UserSentenceRepository
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Assertions.assertThrows
import org.springframework.jdbc.core.JdbcTemplate

@InOutSpringBootTest
class WordServiceIntgTest(
    private val userSentenceRepository: UserSentenceRepository,
    private val wordService: WordService,
    private val jdbcTemplate: JdbcTemplate,
) : DescribeSpec({
        afterEach {
            jdbcTemplate.cleanUp()
        }

        describe("createUserSentence") {
            it("should raise ConflictException if userSentence already exists") {
                // Given
                val userSentenceCreateObject =
                    UserSentenceCreateObject(
                        userId = 1L,
                        wordDefinitionId = 1L,
                        sentenceId = 1L,
                    )
                wordService.createUserSentence(userSentenceCreateObject)

                // When, Then
                assertThrows(ConflictException::class.java) {
                    wordService.createUserSentence(userSentenceCreateObject)
                }
            }

            it("should save and return UserSentence") {
                // Given
                val userSentenceCreateObject =
                    UserSentenceCreateObject(
                        userId = 1L,
                        wordDefinitionId = 1L,
                        sentenceId = 1L,
                    )

                // When
                val sut = wordService.createUserSentence(userSentenceCreateObject)

                // Then
                sut.userId shouldBe userSentenceCreateObject.userId
                sut.wordDefinitionId shouldBe userSentenceCreateObject.wordDefinitionId
                sut.sentenceId shouldBe userSentenceCreateObject.sentenceId
                userSentenceRepository.findByUserIdAndSentenceId(
                    userId = userSentenceCreateObject.userId,
                    sentenceId = userSentenceCreateObject.sentenceId,
                ) shouldBe sut
            }
        }
    })

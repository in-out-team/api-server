package com.inout.apiserver.application.word

import com.inout.apiserver.domain.user.User
import com.inout.apiserver.domain.user.UserFactory
import com.inout.apiserver.domain.word.UserSentenceCreateObject
import com.inout.apiserver.domain.word.WordFactory
import com.inout.apiserver.domain.word.WordService
import com.inout.apiserver.error.ConflictException
import com.inout.apiserver.error.NotFoundException
import com.inout.apiserver.extension.cleanUp
import com.inout.apiserver.helper.InOutSpringBootTest
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.springframework.jdbc.core.JdbcTemplate

@InOutSpringBootTest
class SelectSentenceApplicationTest(
    private val selectSentenceApplication: SelectSentenceApplication,
    private val wordService: WordService,
    private val userFactory: UserFactory,
    private val wordFactory: WordFactory,
    private val jdbcTemplate: JdbcTemplate,
) : DescribeSpec({
        var user: User? = null
        beforeEach {
            user = userFactory.createUser()
        }

        afterEach {
            jdbcTemplate.cleanUp()
        }

        describe("wrong argument provided") {
            it("should raise error if sentenceId does not exist") {
                // given
                val word = wordFactory.createWord()
                val wordDefinitionId = word.definitions.first().id
                val sentenceId = wordFactory.createSentence(wordDefinitionId = wordDefinitionId).id + 1L

                // when
                val exception =
                    shouldThrow<NotFoundException> {
                        selectSentenceApplication.run(sentenceId, user!!)
                    }

                // then
                exception.message shouldBe "Sentence Not Found"
                exception.code shouldBe "SENTENCE_1"
            }
        }

        describe("has already selected sentence") {
            it("should raise error if user has already selected sentence") {
                // given
                val word = wordFactory.createWord()
                val wordDefinitionId = word.definitions.first().id
                val sentence = wordFactory.createSentence(wordDefinitionId = wordDefinitionId)
                val sentenceId = sentence.id
                wordService.createUserSentence(
                    UserSentenceCreateObject(
                        userId = user!!.id,
                        wordDefinitionId = wordDefinitionId,
                        sentenceId = sentenceId,
                    ),
                )

                // when
                val exception =
                    shouldThrow<ConflictException> {
                        selectSentenceApplication.run(sentenceId, user!!)
                    }

                // then
                exception.message shouldBe "User Sentence already exists"
                exception.code shouldBe "SENTENCE_2"
            }
        }

        describe("valid arguments with no selected sentence") {
            it("should create UserSentence") {
                // given
                val word = wordFactory.createWord()
                val wordDefinitionId = word.definitions.first().id
                val sentence = wordFactory.createSentence(wordDefinitionId = wordDefinitionId)
                val sentenceId = sentence.id

                // when
                val userSentence = selectSentenceApplication.run(sentenceId, user!!)

                // then
                userSentence.userId shouldBe user!!.id
                userSentence.wordDefinitionId shouldBe wordDefinitionId
                userSentence.sentenceId shouldBe sentenceId
            }
        }
    })

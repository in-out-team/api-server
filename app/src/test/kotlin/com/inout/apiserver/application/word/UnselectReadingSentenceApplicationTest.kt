package com.inout.apiserver.application.word

import com.inout.apiserver.base.enums.SentenceType
import com.inout.apiserver.domain.user.UserFactory
import com.inout.apiserver.domain.word.UserSentenceCreateObject
import com.inout.apiserver.domain.word.WordFactory
import com.inout.apiserver.domain.word.WordService
import com.inout.apiserver.error.NotFoundException
import com.inout.apiserver.extension.cleanUp
import com.inout.apiserver.helper.InOutSpringBootTest
import com.inout.apiserver.infrastructure.db.user.User
import com.inout.apiserver.infrastructure.db.word.Sentence
import com.inout.apiserver.infrastructure.db.word.Word
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.springframework.jdbc.core.JdbcTemplate

@InOutSpringBootTest
class UnselectReadingSentenceApplicationTest(
    private val subject: UnselectReadingSentenceApplication,
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
            it("should raise error if user has not selected the sentence") {
                // given
                val sentenceId = 1L

                // when
                val exception =
                    shouldThrow<NotFoundException> {
                        subject.run(
                            UnselectReadingSentenceApplication.Request(
                                sentenceId = sentenceId,
                                user = user!!,
                            ),
                        )
                    }

                // then
                exception.message shouldBe "UserSentence not found"
                exception.code shouldBe "SENTENCE_3"
            }
        }

        describe("valid arguments with existing user sentence") {
            var word: Word?
            var sentence: Sentence? = null

            beforeEach {
                word = wordFactory.createWord()
                val wordDefinitionId = word!!.definitions.first().id!!
                sentence = wordFactory.createSentence(wordDefinitionId = wordDefinitionId)
                wordService.createUserSentence(
                    UserSentenceCreateObject(
                        userId = user!!.id!!,
                        wordDefinitionId = sentence!!.wordDefinitionId,
                        type = SentenceType.READING,
                        sentenceId = sentence!!.id!!,
                    ),
                )
            }

            it("should delete user sentence") {
                // when
                subject.run(
                    UnselectReadingSentenceApplication.Request(
                        sentenceId = sentence!!.id!!,
                        user = user!!,
                    ),
                )

                // then
                wordService.getUserSentenceBy(user!!.id!!, sentence!!.id!!) shouldBe null
            }
        }
    })

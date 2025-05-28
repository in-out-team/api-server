package com.inout.apiserver.application.word

import com.inout.apiserver.base.enums.SentenceType
import com.inout.apiserver.domain.study.StudyFactory
import com.inout.apiserver.domain.user.UserFactory
import com.inout.apiserver.domain.word.WordFactory
import com.inout.apiserver.domain.word.WordService
import com.inout.apiserver.error.BadRequestException
import com.inout.apiserver.error.ConflictException
import com.inout.apiserver.error.NotFoundException
import com.inout.apiserver.extension.cleanUp
import com.inout.apiserver.helper.InOutSpringBootTest
import com.inout.apiserver.infrastructure.mongo.user.User
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.MongoTemplate

@InOutSpringBootTest
class SelectReadingSentenceApplicationTest(
    private val subject: SelectReadingSentenceApplication,
    private val wordService: WordService,
    private val userFactory: UserFactory,
    private val wordFactory: WordFactory,
    private val studyFactory: StudyFactory,
    private val mongoTemplate: MongoTemplate,
) : DescribeSpec({
        var user: User? = null

        beforeEach {
            user = userFactory.createUser()
        }

        afterEach {
            mongoTemplate.cleanUp()
        }

        describe("wrong argument provided") {
            it("should raise error if sentenceId does not exist") {
                // given
                val word = wordFactory.createWord()
                val wordDefinitionId = word.definitions.first().id!!
                wordFactory.createSentence(wordDefinitionId = wordDefinitionId)
                val sentenceId = ObjectId()

                // when
                val exception =
                    shouldThrow<NotFoundException> {
                        subject.run(
                            SelectReadingSentenceApplication.Request(
                                sentenceId = sentenceId,
                                user = user!!,
                            ),
                        )
                    }

                // then
                exception.message shouldBe "Sentence Not Found"
                exception.code shouldBe "SENTENCE_1"
            }
        }

        describe("user is not studying word") {
            it("should raise error if user is not studying given wordDefinitionId") {
                // given
                val word = wordFactory.createWord()
                val wordDefinitionId = word.definitions.first().id!!
                val sentence = wordFactory.createSentence(wordDefinitionId = wordDefinitionId)
                val sentenceId = sentence.id!!

                // when
                val exception =
                    shouldThrow<BadRequestException> {
                        subject.run(
                            SelectReadingSentenceApplication.Request(
                                sentenceId = sentenceId,
                                user = user!!,
                            ),
                        )
                    }

                // then
                exception.message shouldBe "User is not studying given wordDefinitionId of $wordDefinitionId"
                exception.code shouldBe "SENTENCE_10"
            }
        }

        describe("user is studying word") {
            beforeEach {
                studyFactory.createStudy(
                    userId = user!!.id!!,
                    wordDefinitionId =
                        wordFactory
                            .createWord()
                            .definitions
                            .first()
                            .id!!,
                )
            }

            describe("has already selected sentence") {
                it("should raise error if user has already selected sentence") {
                    // given
                    val word = wordFactory.createWord()
                    val wordDefinitionId = word.definitions.first().id!!
                    val sentence = wordFactory.createSentence(wordDefinitionId = wordDefinitionId)
                    val sentenceId = sentence.id!!
                    wordService.createUserSentence(
                        userId = user!!.id!!,
                        wordDefinitionId = wordDefinitionId,
                        type = SentenceType.READING,
                        sentenceId = sentenceId,
                    )

                    // when
                    val exception =
                        shouldThrow<ConflictException> {
                            subject.run(
                                SelectReadingSentenceApplication.Request(
                                    sentenceId = sentenceId,
                                    user = user!!,
                                ),
                            )
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
                    val wordDefinitionId = word.definitions.first().id!!
                    val sentence = wordFactory.createSentence(wordDefinitionId = wordDefinitionId)
                    val sentenceId = sentence.id!!

                    // when
                    val userSentence =
                        subject
                            .run(
                                SelectReadingSentenceApplication.Request(
                                    sentenceId = sentenceId,
                                    user = user!!,
                                ),
                            ).userSentence

                    // then
                    userSentence.userId shouldBe user!!.id
                    userSentence.wordDefinitionId shouldBe wordDefinitionId
                    userSentence.sentenceId shouldBe sentenceId
                }
            }
        }
    })

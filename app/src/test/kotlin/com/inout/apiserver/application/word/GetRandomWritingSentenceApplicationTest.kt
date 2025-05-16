package com.inout.apiserver.application.word

import com.inout.apiserver.base.enums.SentenceType
import com.inout.apiserver.domain.study.MongoStudyFactory
import com.inout.apiserver.domain.user.MongoUserFactory
import com.inout.apiserver.domain.word.MongoWordFactory
import com.inout.apiserver.domain.word.WordWithDefinitions
import com.inout.apiserver.error.BadRequestException
import com.inout.apiserver.error.NotFoundException
import com.inout.apiserver.extension.cleanUp
import com.inout.apiserver.helper.InOutSpringBootTest
import com.inout.apiserver.infrastructure.mongo.user.MongoUser
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.MongoTemplate

@InOutSpringBootTest
class GetRandomWritingSentenceApplicationTest(
    private val subject: GetRandomWritingSentenceApplication,
    // factories
    private val userFactory: MongoUserFactory,
    private val wordFactory: MongoWordFactory,
    private val studyFactory: MongoStudyFactory,
    // etc
    private val mongoTemplate: MongoTemplate,
) : DescribeSpec({
        var user: MongoUser? = null

        beforeEach {
            user = userFactory.createUser()
        }

        afterEach {
            mongoTemplate.cleanUp()
        }

        describe("when there are no writing sentences") {
            var word: WordWithDefinitions? = null

            beforeEach {
                word = wordFactory.createWord()
            }

            it("throws NotFoundException") {
                // given
                val wordDefinitionId = word!!.definitions.first().id!!
                wordFactory.createSentence(
                    wordDefinitionId = wordDefinitionId,
                    type = SentenceType.READING,
                )

                // when
                val result =
                    shouldThrow<NotFoundException> {
                        subject.run(
                            GetRandomWritingSentenceApplication.Request(
                                wordDefinitionId = wordDefinitionId,
                                user = user!!,
                            ),
                        )
                    }

                // then
                result.message shouldBe "Sentences not found for word definition id: $wordDefinitionId"
                result.code shouldBe "SENTENCE_1"
            }
        }

        describe("when user already has enough sentences") {
            var word: WordWithDefinitions?
            var wordDefinitionId = ObjectId()

            beforeEach {
                word = wordFactory.createWord()
                wordDefinitionId = word!!.definitions.first().id!!
                studyFactory.createStudy(
                    userId = user!!.id!!,
                    wordDefinitionId = wordDefinitionId,
                )
                repeat(4) {
                    val sentence =
                        wordFactory.createSentence(
                            wordDefinitionId = wordDefinitionId,
                            type = SentenceType.WRITING,
                        )

                    if (it == 0) {
                        return@repeat
                    }

                    wordFactory.createUserSentence(
                        userId = user!!.id!!,
                        wordDefinitionId = wordDefinitionId,
                        sentenceId = sentence.id!!,
                    )
                }
            }

            it("throws BadRequestException") {
                // when
                val result =
                    shouldThrow<BadRequestException> {
                        subject.run(
                            GetRandomWritingSentenceApplication.Request(
                                wordDefinitionId = wordDefinitionId,
                                user = user!!,
                            ),
                        )
                    }

                // then
                result.message shouldBe "User already has enough sentences"
                result.code shouldBe "SENTENCE_6"
            }
        }

        describe("when all sentences are already selected") {
            var word: WordWithDefinitions?
            var wordDefinitionId = ObjectId()

            beforeEach {
                word = wordFactory.createWord()
                wordDefinitionId = word!!.definitions.first().id!!
                repeat(2) {
                    val sentence =
                        wordFactory.createSentence(
                            wordDefinitionId = wordDefinitionId,
                            type = SentenceType.WRITING,
                        )

                    wordFactory.createUserSentence(
                        userId = user!!.id!!,
                        wordDefinitionId = wordDefinitionId,
                        sentenceId = sentence.id!!,
                    )
                }
            }

            it("throws BadRequestException") {
                // when
                val result =
                    shouldThrow<BadRequestException> {
                        subject.run(
                            GetRandomWritingSentenceApplication.Request(
                                wordDefinitionId = wordDefinitionId,
                                user = user!!,
                            ),
                        )
                    }

                // then
                result.message shouldBe "All sentences are already selected"
                result.code shouldBe "SENTENCE_7"
            }
        }

        describe("when there are available sentences") {
            var word: WordWithDefinitions?
            var wordDefinitionId = ObjectId()

            beforeEach {
                word = wordFactory.createWord()
                wordDefinitionId = word!!.definitions.first().id!!
                repeat(2) {
                    wordFactory.createSentence(
                        wordDefinitionId = wordDefinitionId,
                        type = SentenceType.WRITING,
                    )
                }
            }

            it("returns a random sentence") {
                // when
                val result =
                    subject.run(
                        GetRandomWritingSentenceApplication.Request(
                            wordDefinitionId = wordDefinitionId,
                            user = user!!,
                        ),
                    )

                // then
                result.sentence.wordDefinitionId shouldBe wordDefinitionId
                result.sentence.type shouldBe SentenceType.WRITING
            }
        }
    })

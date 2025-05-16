package com.inout.apiserver.application.word

import com.inout.apiserver.base.enums.SentenceType
import com.inout.apiserver.domain.user.MongoUserFactory
import com.inout.apiserver.domain.word.MongoWordFactory
import com.inout.apiserver.domain.word.MongoWordService
import com.inout.apiserver.domain.word.WordWithDefinitions
import com.inout.apiserver.error.NotFoundException
import com.inout.apiserver.extension.cleanUp
import com.inout.apiserver.helper.InOutSpringBootTest
import com.inout.apiserver.infrastructure.mongo.user.MongoUser
import com.inout.apiserver.infrastructure.mongo.word.MongoSentence
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.MongoTemplate

@InOutSpringBootTest
class UnselectReadingSentenceApplicationTest(
    private val subject: UnselectReadingSentenceApplication,
    private val wordService: MongoWordService,
    private val userFactory: MongoUserFactory,
    private val wordFactory: MongoWordFactory,
    private val mongoTemplate: MongoTemplate,
) : DescribeSpec({
        var user: MongoUser? = null

        beforeEach {
            user = userFactory.createUser()
        }

        afterEach {
            mongoTemplate.cleanUp()
        }

        describe("wrong argument provided") {
            it("should raise error if user has not selected the sentence") {
                // given
                val sentenceId = ObjectId()

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
            var word: WordWithDefinitions?
            var sentence: MongoSentence? = null

            beforeEach {
                word = wordFactory.createWord()
                val wordDefinitionId = word!!.definitions.first().id!!
                sentence = wordFactory.createSentence(wordDefinitionId = wordDefinitionId)
                wordService.createUserSentence(
                    userId = user!!.id!!,
                    wordDefinitionId = sentence!!.wordDefinitionId,
                    type = SentenceType.READING,
                    sentenceId = sentence!!.id!!,
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

package com.inout.apiserver.application.word

import com.inout.apiserver.base.enums.SentenceType
import com.inout.apiserver.domain.user.MongoUserFactory
import com.inout.apiserver.domain.word.MongoWordFactory
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
class GetWritingSentenceFeedbacksApplicationTest(
    private val subject: GetWritingSentenceFeedbacksApplication,
    // factories
    private val wordFactory: MongoWordFactory,
    private val userFactory: MongoUserFactory,
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

        describe("when sentence does not exists") {
            it("should throw NotFoundException") {
                // given
                val sentenceId = ObjectId()
                val request =
                    GetWritingSentenceFeedbacksApplication.Request(
                        sentenceId = sentenceId,
                        user = user!!,
                    )

                // when
                val result =
                    shouldThrow<NotFoundException> {
                        subject.run(request)
                    }

                // then
                result.message shouldBe "Sentence not found for id: $sentenceId"
                result.code shouldBe "SENTENCE_3"
            }
        }

        describe("when sentence exists") {
            var word: WordWithDefinitions?
            var wordDefinition: WordWithDefinitions.WordDefinition? = null
            var sentence: MongoSentence? = null

            beforeEach {
                word = wordFactory.createWord()
                wordDefinition = word!!.definitions.first()
                sentence =
                    wordFactory.createSentence(wordDefinitionId = wordDefinition!!.id!!, type = SentenceType.WRITING)
            }

            it("should return empty feedbacks when user does not have userSentence") {
                // given
                val request =
                    GetWritingSentenceFeedbacksApplication.Request(
                        sentenceId = sentence!!.id!!,
                        user = user!!,
                    )

                // when
                val result = subject.run(request)

                // then
                result.feedbacks.isEmpty() shouldBe true
            }

            it("should return feedbacks in descending order of createdAt") {
                // given
                val userSentence = wordFactory.createUserSentence(user!!.id!!, wordDefinition!!.id!!, sentence!!.id!!)
                val sentenceFeedback1 =
                    wordFactory.createSentenceFeedback(
                        sentenceId = sentence!!.id!!,
                        submittedContent = "I read book",
                        feedback = "주어와 동사 사이에 'a'를 넣어야 합니다. 'a'는 무언가 특정한 책을 가리키는데 도움을 줍니다. 모호함을 없애고 명확한 문장을 만들기 위해 필요한 내용입니다.",
                    )
                val sentenceFeedback2 =
                    wordFactory.createSentenceFeedback(
                        sentenceId = sentence!!.id!!,
                        submittedContent = "I reading a book",
                        feedback = "동사와 목적어 사이에 'am'을 넣어야 합니다. 'am'은 현재 진행형을 나타냅니다.",
                    )
                wordFactory.createUserSentenceFeedback(userSentence.id!!, sentenceFeedback1.id!!)
                wordFactory.createUserSentenceFeedback(userSentence.id!!, sentenceFeedback2.id!!)

                // when
                val request =
                    GetWritingSentenceFeedbacksApplication.Request(
                        sentenceId = sentence!!.id!!,
                        user = user!!,
                    )
                val result = subject.run(request)

                // then
                result.feedbacks.size shouldBe 2
                result.feedbacks[0] shouldBe sentenceFeedback2
                result.feedbacks[1] shouldBe sentenceFeedback1
            }
        }
    })

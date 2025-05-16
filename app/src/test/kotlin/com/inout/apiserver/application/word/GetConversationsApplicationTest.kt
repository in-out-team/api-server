package com.inout.apiserver.application.word

import com.inout.apiserver.domain.study.MongoStudyFactory
import com.inout.apiserver.domain.user.MongoUserFactory
import com.inout.apiserver.domain.word.MongoWordFactory
import com.inout.apiserver.domain.word.WordWithDefinitions
import com.inout.apiserver.error.BadRequestException
import com.inout.apiserver.extension.cleanUp
import com.inout.apiserver.helper.InOutSpringBootTest
import com.inout.apiserver.infrastructure.mongo.user.MongoUser
import com.inout.apiserver.infrastructure.mongo.word.MongoConversationRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.MongoTemplate

@InOutSpringBootTest
class GetConversationsApplicationTest(
    private val subject: GetConversationsApplication,
    // repositories
    private val conversationRepository: MongoConversationRepository,
    // factories
    private val userFactory: MongoUserFactory,
    private val wordFactory: MongoWordFactory,
    private val studyFactory: MongoStudyFactory,
    // etc
    private val mongoTemplate: MongoTemplate,
) : DescribeSpec({
        var user: MongoUser? = null
        var word: WordWithDefinitions? = null

        beforeEach {
            user = userFactory.createUser()
            word = wordFactory.createWord()
        }

        afterEach {
            mongoTemplate.cleanUp()
        }

        describe("when user is not studying given wordDefinitionId") {
            var wordDefinitionId = ObjectId()

            beforeEach {
                wordDefinitionId = word!!.definitions.first().id!!
                conversationRepository
                    .findAllByUserIdAndWordDefinitionId(user!!.id!!, wordDefinitionId)
                    .isEmpty() shouldBe true
            }

            it("should raise error") {
                // when
                val exception =
                    shouldThrow<BadRequestException> {
                        subject.run(
                            GetConversationsApplication.Request(
                                user = user!!,
                                wordDefinitionId = wordDefinitionId,
                            ),
                        )
                    }

                // then
                exception.message shouldBe "User is not studying given wordDefinitionId of $wordDefinitionId"
                exception.code shouldBe "CONVERSATION_1"
            }
        }

        describe("when user is studying given wordDefinitionId") {
            var wordDefinitionId = ObjectId()

            beforeEach {
                wordDefinitionId = word!!.definitions.first().id!!
                studyFactory.createStudy(user!!.id!!, wordDefinitionId)
            }

            it("should return empty list of conversations when no conversations found") {
                // when
                val result =
                    subject.run(
                        GetConversationsApplication.Request(
                            user = user!!,
                            wordDefinitionId = wordDefinitionId,
                        ),
                    )

                // then
                result.conversations.size shouldBe 0
            }

            it("should return list of conversations when conversations found") {
                // given
                val conversation = wordFactory.createConversation(user!!, wordDefinitionId)

                // when
                val result =
                    subject.run(
                        GetConversationsApplication.Request(
                            user = user!!,
                            wordDefinitionId = wordDefinitionId,
                        ),
                    )

                // then
                result.conversations.size shouldBe 1
                result.conversations.first().id shouldBe conversation.id
            }
        }
    })

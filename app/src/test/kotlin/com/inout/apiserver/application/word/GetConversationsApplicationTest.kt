package com.inout.apiserver.application.word

import com.inout.apiserver.domain.study.StudyFactory
import com.inout.apiserver.domain.user.User
import com.inout.apiserver.domain.user.UserFactory
import com.inout.apiserver.domain.word.Word
import com.inout.apiserver.domain.word.WordFactory
import com.inout.apiserver.error.BadRequestException
import com.inout.apiserver.extension.cleanUp
import com.inout.apiserver.helper.InOutSpringBootTest
import com.inout.apiserver.infrastructure.db.word.ConversationRepository
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.assertThrows
import org.springframework.jdbc.core.JdbcTemplate

@InOutSpringBootTest
class GetConversationsApplicationTest(
    private val subject: GetConversationsApplication,
    // repositories
    private val conversationRepository: ConversationRepository,
    // factories
    private val userFactory: UserFactory,
    private val wordFactory: WordFactory,
    private val studyFactory: StudyFactory,
    // etc
    private val jdbcTemplate: JdbcTemplate,
) : DescribeSpec({
        var user: User? = null
        var word: Word? = null

        beforeEach {
            user = userFactory.createUser()
            word = wordFactory.createWord()
        }

        afterEach {
            jdbcTemplate.cleanUp()
        }

        describe("when user is not studying given wordDefinitionId") {
            var wordDefinitionId = 0L

            beforeEach {
                wordDefinitionId = word!!.definitions.first().id
                conversationRepository
                    .findAllByUserIdAndWordDefinitionId(user!!.id, wordDefinitionId)
                    .isEmpty() shouldBe true
            }

            it("should raise error") {
                // when
                val exception = assertThrows<BadRequestException> { subject.run(wordDefinitionId, user!!) }

                // then
                exception.message shouldBe "User is not studying given wordDefinitionId of $wordDefinitionId"
                exception.code shouldBe "CONVERSATION_1"
            }
        }

        describe("when user is studying given wordDefinitionId") {
            var wordDefinitionId = 0L

            beforeEach {
                wordDefinitionId = word!!.definitions.first().id
                studyFactory.createStudy(user!!.id, wordDefinitionId)
            }

            it("should return empty list of conversations when no conversations found") {
                // when
                val result = subject.run(wordDefinitionId, user!!)

                // then
                result.conversations.size shouldBe 0
            }

            it("should return list of conversations when conversations found") {
                // given
                val conversation = wordFactory.createConversation(user!!.id, wordDefinitionId)

                // when
                val result = subject.run(wordDefinitionId, user!!)

                // then
                result.conversations.size shouldBe 1
                result.conversations.first().id shouldBe conversation.id
            }
        }
    })

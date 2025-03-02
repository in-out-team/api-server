package com.inout.apiserver.application.word

import com.inout.apiserver.base.enums.SenderType
import com.inout.apiserver.base.service.openai.OpenAIService
import com.inout.apiserver.domain.user.UserFactory
import com.inout.apiserver.domain.word.AudioFactory
import com.inout.apiserver.domain.word.WordAIService
import com.inout.apiserver.domain.word.WordFactory
import com.inout.apiserver.domain.word.WordService
import com.inout.apiserver.error.BadRequestException
import com.inout.apiserver.error.NotFoundException
import com.inout.apiserver.extension.cleanUp
import com.inout.apiserver.helper.InOutSpringBootTest
import com.inout.apiserver.infrastructure.db.user.User
import com.inout.apiserver.infrastructure.db.word.Conversation
import com.inout.apiserver.infrastructure.db.word.ConversationMessage
import com.inout.apiserver.infrastructure.db.word.ConversationRepository
import com.inout.apiserver.infrastructure.db.word.Word
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.jdbc.core.JdbcTemplate

@InOutSpringBootTest
class RespondToConversationApplicationTest(
    private val subject: RespondToConversationApplication,
    // services
    private val wordService: WordService,
    @SpyBean
    private val openAIService: OpenAIService,
    @SpyBean
    private val wordAIService: WordAIService,
    // factories
    private val userFactory: UserFactory,
    private val wordFactory: WordFactory,
    private val audioFactory: AudioFactory,
    // repositories
    private val conversationRepository: ConversationRepository,
    // etc
    private val jdbcTemplate: JdbcTemplate,
) : DescribeSpec({
        var user: User? = null
        var word: Word?
        var wordDefinitionId = 0L

        beforeEach {
            user = userFactory.createUser()
            word = wordFactory.createWord()
            wordDefinitionId = word!!.definitions.first().id!!

            doReturn("response")
                .`when`(openAIService)
                .fetchConversation(any(), any(), any(), any(), any())
            doReturn(audioFactory.createAudio())
                .`when`(wordAIService)
                .findOrCreateAudio(any(), any())
        }

        afterEach {
            jdbcTemplate.cleanUp()
        }

        fun createConversation(
            user: User,
            wordDefinitionId: Long,
        ): Conversation {
            val audio = audioFactory.createAudio()
            return wordFactory
                .createConversation(userId = user.id!!, wordDefinitionId = wordDefinitionId)
                .let {
                    conversationRepository.save(
                        it.copy(
                            messages =
                                mutableListOf(
                                    ConversationMessage(
                                        sender = SenderType.SYSTEM,
                                        content = audio.content,
                                        audio = audio,
                                    ),
                                ),
                        ),
                    )
                }
        }

        describe("when conversation does not exist") {
            it("should throw NotFoundException if conversation actually does not exist") {
                // given
                wordService.getConversationById(1L) shouldBe null
                val request =
                    RespondToConversationApplication.Request(
                        conversationId = 1L,
                        responseMessage = "response",
                        user = user!!,
                    )

                // when
                val exception = shouldThrow<NotFoundException> { subject.run(request) }

                // then
                exception.message shouldBe "Conversation not found"
                exception.code shouldBe "CONVERSATION_3"
            }

            it("should throw NotFoundException if conversation exists, but user is not the owner") {
                // given
                val conversation = createConversation(user!!, wordDefinitionId)
                val otherUser = userFactory.createUser(email = "test2@1.com")
                val request =
                    RespondToConversationApplication.Request(
                        conversationId = conversation.id!!,
                        responseMessage = "response",
                        user = otherUser,
                    )

                // when
                val exception = shouldThrow<NotFoundException> { subject.run(request) }

                // then
                exception.message shouldBe "Conversation not found"
                exception.code shouldBe "CONVERSATION_3"
            }
        }

        describe("when conversation of given conversationId exists") {
            var conversation: Conversation? = null

            beforeEach {
                conversation = createConversation(user!!, wordDefinitionId)
            }

            it("should throw BadRequestException if system response limit exceeded") {
                // given
                repeat(3) {
                    conversation!!.addUserMessage("user message")
                    conversation!!.addSystemMessage("system message", audioFactory.createAudio())
                }
                conversationRepository.save(conversation!!)
                val request =
                    RespondToConversationApplication.Request(
                        conversationId = conversation!!.id!!,
                        responseMessage = "response",
                        user = user!!,
                    )

                // when
                val exception = shouldThrow<BadRequestException> { subject.run(request) }

                // then
                exception.message shouldBe "System response limit exceeded"
                exception.code shouldBe "CONVERSATION_4"
            }

            it("should raise BadRequestException if system has not responded yet") {
                // given
                conversation!!.addUserMessage("user message")
                conversationRepository.save(conversation!!)
                val request =
                    RespondToConversationApplication.Request(
                        conversationId = conversation!!.id!!,
                        responseMessage = "response",
                        user = user!!,
                    )

                // when
                val exception = shouldThrow<BadRequestException> { subject.run(request) }

                // then
                exception.message shouldBe "System has not responded yet"
                exception.code shouldBe "CONVERSATION_5"
            }

            it("should return updated conversation which includes user and system messages") {
                // given
                conversation!!.messages.size shouldBe 1
                conversation!!.messages[0].sender shouldBe SenderType.SYSTEM
                val request =
                    RespondToConversationApplication.Request(
                        conversationId = conversation!!.id!!,
                        responseMessage = "response",
                        user = user!!,
                    )

                // when
                val response = subject.run(request)

                // then
                response.updatedConversation.messages.size shouldBe 3
                response.updatedConversation.messages[1].sender shouldBe SenderType.USER
                response.updatedConversation.messages[1].content shouldBe "response"
                response.updatedConversation.messages[1].audio shouldBe null
                response.updatedConversation.messages[2].sender shouldBe SenderType.SYSTEM
                response.updatedConversation.messages[2].content shouldBe "response"
                response.updatedConversation.messages[2].audio shouldNotBe null
            }
        }
    })

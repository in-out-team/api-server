package com.inout.apiserver.application.word

import com.inout.apiserver.base.enums.SenderType
import com.inout.apiserver.base.service.FeedbackService
import com.inout.apiserver.domain.user.UserFactory
import com.inout.apiserver.domain.word.AudioAIService
import com.inout.apiserver.domain.word.AudioFactory
import com.inout.apiserver.domain.word.ConversationWithMessages
import com.inout.apiserver.domain.word.WordFactory
import com.inout.apiserver.domain.word.WordService
import com.inout.apiserver.domain.word.WordWithDefinitions
import com.inout.apiserver.error.BadRequestException
import com.inout.apiserver.error.NotFoundException
import com.inout.apiserver.extension.cleanUp
import com.inout.apiserver.helper.InOutSpringBootTest
import com.inout.apiserver.infrastructure.mongo.user.User
import com.inout.apiserver.infrastructure.mongo.word.ConversationMessage
import com.inout.apiserver.infrastructure.mongo.word.ConversationRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.bson.types.ObjectId
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.whenever
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.data.mongodb.core.MongoTemplate

@InOutSpringBootTest
class RespondToConversationApplicationTest(
    private val subject: RespondToConversationApplication,
    // services
    private val wordService: WordService,
    @SpyBean
    private val feedbackService: FeedbackService,
    @SpyBean
    private val audioAIService: AudioAIService,
    // factories
    private val userFactory: UserFactory,
    private val wordFactory: WordFactory,
    private val audioFactory: AudioFactory,
    // repositories
    private val conversationRepository: ConversationRepository,
    // etc
    private val mongoTemplate: MongoTemplate,
) : DescribeSpec({
        var user: User? = null
        var word: WordWithDefinitions?
        var wordDefinitionId = ObjectId()

        beforeEach {
            user = userFactory.createUser()
            word = wordFactory.createWord()
            wordDefinitionId = word!!.definitions.first().id!!

            doReturn("response")
                .whenever(feedbackService)
                .fetchConversationFeedback(any(), any(), any(), any(), any())
            doReturn(audioFactory.createAudio())
                .whenever(audioAIService)
                .findOrCreateAudio(any(), any(), any())
        }

        afterEach {
            mongoTemplate.cleanUp()
        }

        fun createConversation(
            user: User,
            wordDefinitionId: ObjectId,
        ): ConversationWithMessages {
            val audio = audioFactory.createAudio()
            val conversation = wordFactory.createConversation(user = user, wordDefinitionId = wordDefinitionId)
            val conversationMessage =
                ConversationMessage(
                    conversationId = conversation.id!!,
                    sender = SenderType.SYSTEM,
                    content = "system message",
                    audio = audio,
                )
            conversationRepository.saveConversationMessage(conversationMessage)

            return wordService.getConversationById(conversation.id!!)!!
        }

        describe("when conversation does not exist") {
            it("should throw NotFoundException if conversation actually does not exist") {
                // given
                val conversationId = ObjectId()
                wordService.getConversationById(conversationId) shouldBe null
                val request =
                    RespondToConversationApplication.Request(
                        conversationId = conversationId,
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
            var conversation: ConversationWithMessages? = null

            beforeEach {
                conversation = createConversation(user!!, wordDefinitionId)
            }

            it("should throw BadRequestException if system response limit exceeded") {
                // given
                repeat(3) {
                    wordService.doConversation(
                        conversation = conversation!!,
                        userResponseMessage = "user message",
                        systemResponseMessage = "system message",
                        systemResponseAudio = audioFactory.createAudio(),
                    )
                }

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
                conversationRepository.saveConversationMessage(
                    ConversationMessage(
                        conversationId = conversation!!.id!!,
                        sender = SenderType.USER,
                        content = "user message",
                    ),
                )
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

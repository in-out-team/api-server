package com.inout.apiserver.application.word

import com.inout.apiserver.base.enums.SenderType
import com.inout.apiserver.domain.study.StudyFactory
import com.inout.apiserver.domain.study.StudyService
import com.inout.apiserver.domain.user.UserFactory
import com.inout.apiserver.domain.word.AudioFactory
import com.inout.apiserver.domain.word.WordAIService
import com.inout.apiserver.domain.word.WordFactory
import com.inout.apiserver.error.BadRequestException
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
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.data.domain.PageRequest
import org.springframework.jdbc.core.JdbcTemplate

@InOutSpringBootTest
class StartConversationApplicationTest(
    private val subject: StartConversationApplication,
    // services
    private val studyService: StudyService,
    @SpyBean
    private val wordAIService: WordAIService,
    // repositories
    private val conversationRepository: ConversationRepository,
    // factories
    private val audioFactory: AudioFactory,
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

            // mock calls to wordAIService to save and return a dummy audio
            doReturn(audioFactory.createAudio())
                .`when`(wordAIService)
                .findOrCreateAudio(any(), any())
        }

        afterEach {
            jdbcTemplate.cleanUp()
        }

        describe("when user is not studying given wordDefinitionId") {
            beforeEach {
                studyService.getAllByUserId(user!!.id!!, PageRequest.of(0, 10)).totalElements shouldBe 0
            }

            it("should throw BadRequestException") {
                // given
                val wordDefinitionId = word!!.definitions.first().id!!

                // when
                val result =
                    shouldThrow<BadRequestException> {
                        subject.run(
                            StartConversationApplication.Request(
                                wordDefinitionId = wordDefinitionId,
                                user = user!!,
                            ),
                        )
                    }

                // then
                result.message shouldBe "User is not studying given wordDefinitionId of $wordDefinitionId"
                result.code shouldBe "CONVERSATION_1"
            }
        }

        describe("when user is studying given wordDefinitionId") {
            var wordDefinitionId = 0L

            beforeEach {
                wordDefinitionId = word!!.definitions.first().id!!
                studyFactory.createStudy(userId = user!!.id!!, wordDefinitionId = wordDefinitionId)
            }

            describe("when user has existing conversation/s") {
                val conversations = mutableListOf<Conversation>()

                beforeEach {
                    conversations.add(wordFactory.createConversation(user!!.id!!, wordDefinitionId))
                    conversations.add(wordFactory.createConversation(user!!.id!!, wordDefinitionId))
                    conversations.add(wordFactory.createConversation(user!!.id!!, wordDefinitionId))
                }

                fun populateConversation(conversation: Conversation) {
                    conversationRepository.save(
                        conversation.apply {
                            val audio = audioFactory.createAudio()
                            messages.add(
                                ConversationMessage(
                                    sender = SenderType.SYSTEM,
                                    content = audio.content,
                                    audio = audio,
                                ),
                            )
                            messages.add(
                                ConversationMessage(
                                    sender = SenderType.USER,
                                    content = "Hello",
                                ),
                            )
                        },
                    )
                }

                it("should return the first conversation with no user messages") {
                    // when
                    val firstConversation = conversations.first()
                    populateConversation(firstConversation)
                    val result =
                        subject.run(
                            StartConversationApplication.Request(
                                wordDefinitionId = wordDefinitionId,
                                user = user!!,
                            ),
                        )

                    // then
                    val secondConversation = conversations[1]
                    result.conversation.id shouldBe secondConversation.id
                }

                it("should throw BadRequestException when user has reached maximum number of conversations") {
                    // when
                    conversations.forEach { populateConversation(it) }
                    val result =
                        shouldThrow<BadRequestException> {
                            subject.run(
                                StartConversationApplication.Request(
                                    wordDefinitionId = wordDefinitionId,
                                    user = user!!,
                                ),
                            )
                        }

                    // then
                    result.message shouldBe "Reached maximum number of conversations"
                    result.code shouldBe "CONVERSATION_2"
                }
            }

            describe("when new conversation can be started") {
                beforeEach {
                    conversationRepository
                        .findAllByUserIdAndWordDefinitionId(
                            user!!.id!!,
                            wordDefinitionId,
                        ).size shouldBe 0
                }

                it("should create a new conversation") {
                    // when
                    val result =
                        subject.run(
                            StartConversationApplication.Request(
                                wordDefinitionId = wordDefinitionId,
                                user = user!!,
                            ),
                        )

                    // then
                    conversationRepository
                        .findAllByUserIdAndWordDefinitionId(
                            user!!.id!!,
                            wordDefinitionId,
                        ).size shouldBe 1
                    result.conversation.userId shouldBe user!!.id
                    result.conversation.wordDefinitionId shouldBe wordDefinitionId
                    result.conversation.messages.size shouldBe 1
                }
            }
        }
    })

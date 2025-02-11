package com.inout.apiserver.application.word

import com.inout.apiserver.base.enums.SentenceType
import com.inout.apiserver.base.service.openai.OpenAIService
import com.inout.apiserver.base.service.openai.dto.OpenAIWritingSentenceFeedbackResponse
import com.inout.apiserver.domain.user.UserFactory
import com.inout.apiserver.domain.word.Sentence
import com.inout.apiserver.domain.word.Word
import com.inout.apiserver.domain.word.WordDefinition
import com.inout.apiserver.domain.word.WordFactory
import com.inout.apiserver.domain.word.WordService
import com.inout.apiserver.error.BadRequestException
import com.inout.apiserver.error.NotFoundException
import com.inout.apiserver.extension.cleanUp
import com.inout.apiserver.helper.InOutSpringBootTest
import com.inout.apiserver.infrastructure.db.user.User
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.mockito.kotlin.any
import org.mockito.kotlin.clearInvocations
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.jdbc.core.JdbcTemplate

@InOutSpringBootTest
class GetWritingSentenceFeedbackApplicationTest(
    private val getWritingSentenceFeedbackApplication: GetWritingSentenceFeedbackApplication,
    // services
    @SpyBean
    private val openAIService: OpenAIService,
    @SpyBean
    private val wordService: WordService,
    // factories
    private val wordFactory: WordFactory,
    private val userFactory: UserFactory,
    // etc
    private val jdbcTemplate: JdbcTemplate,
) : DescribeSpec({

        var user: User? = null
        afterEach {
            jdbcTemplate.cleanUp()
        }

        beforeEach {
            user = userFactory.createUser()
        }

        describe("when sentence is not found") {
            it("should raise NotFoundException") {
                // given
                val sentenceId = 0L
                val submittedContent = "submitted content"

                // when
                val exception =
                    shouldThrow<NotFoundException> {
                        getWritingSentenceFeedbackApplication.run(
                            GetWritingSentenceFeedbackApplication.Request(
                                user = user!!,
                                sentenceId = sentenceId,
                                submittedContent = submittedContent,
                            ),
                        )
                    }

                // then
                exception.message shouldBe "Sentence not found for id: $sentenceId"
                exception.code shouldBe "SENTENCE_3"
            }
        }

        describe("when sentence is found") {
            var word: Word?
            var wordDefinition: WordDefinition? = null
            var sentence: Sentence? = null

            beforeEach {
                word = wordFactory.createWord()
                wordDefinition = word!!.definitions.first()
                sentence = wordFactory.createSentence(wordDefinition!!.id, type = SentenceType.WRITING)
            }

            describe("providing correct answer") {
                it("should respond accordingly") {
                    // given
                    val submittedContent = sentence!!.content

                    // when
                    val response =
                        getWritingSentenceFeedbackApplication.run(
                            GetWritingSentenceFeedbackApplication.Request(
                                user = user!!,
                                sentenceId = sentence!!.id,
                                submittedContent = submittedContent,
                            ),
                        )

                    // then
                    response.feedback shouldBe "정답입니다!"
                }
            }

            describe("providing incorrect answer") {
                fun mockOpenAIRequest() {
                    doReturn(
                        OpenAIWritingSentenceFeedbackResponse(
                            feedback = "feedback",
                        ),
                    ).`when`(openAIService)
                        .fetchWritingSentenceFeedback(any(), any(), any(), any())
                }

                afterEach {
                    clearInvocations(openAIService)
                }

                it("should return existing sentence feedback if submitted content was already attempted") {
                    // given
                    val existingSentenceFeedback = wordFactory.createSentenceFeedback(sentenceId = sentence!!.id)

                    // when
                    val response =
                        getWritingSentenceFeedbackApplication.run(
                            GetWritingSentenceFeedbackApplication.Request(
                                user = user!!,
                                sentenceId = sentence!!.id,
                                submittedContent = existingSentenceFeedback.submittedContent,
                            ),
                        )

                    // then
                    response.feedback shouldBe existingSentenceFeedback.feedback
                    verify(openAIService, times(0)).fetchWritingSentenceFeedback(any(), any(), any(), any())
                    verify(wordService, times(0)).createSentenceFeedback(any())
                }

                it("should raise error if max attempt is reached") {
                    // given
                    val userSentence = wordFactory.createUserSentence(user!!.id!!, wordDefinition!!.id, sentence!!.id)
                    repeat(3) {
                        val sentenceFeedback =
                            wordFactory.createSentenceFeedback(
                                sentenceId = sentence!!.id,
                                submittedContent = "I read a book $it",
                                feedback = "feedback",
                            )
                        wordFactory.createUserSentenceFeedback(
                            userSentenceId = userSentence.id,
                            sentenceFeedbackId = sentenceFeedback.id,
                        )
                    }

                    // when
                    val exception =
                        shouldThrow<BadRequestException> {
                            getWritingSentenceFeedbackApplication.run(
                                GetWritingSentenceFeedbackApplication.Request(
                                    user = user!!,
                                    sentenceId = sentence!!.id,
                                    submittedContent = "test",
                                ),
                            )
                        }

                    // then
                    exception.message shouldBe "Maximum attempts on getting writing sentence feedback has been reached"
                    exception.code shouldBe "SENTENCE_8"
                }

                it("should create userSentence on first attempt") {
                    // given
                    mockOpenAIRequest()
                    wordService.getUserSentenceBy(user!!.id!!, sentence!!.id) shouldBe null

                    // when
                    getWritingSentenceFeedbackApplication.run(
                        GetWritingSentenceFeedbackApplication.Request(
                            user = user!!,
                            sentenceId = sentence!!.id,
                            submittedContent = "test",
                        ),
                    )

                    // then
                    wordService.getUserSentenceBy(user!!.id!!, sentence!!.id) shouldNotBe null
                }

                it("should create sentenceFeedback if submitted content was not attempted") {
                    // given
                    mockOpenAIRequest()
                    wordService.getSentenceFeedbackBy(sentence!!.id, "test") shouldBe null

                    // when
                    getWritingSentenceFeedbackApplication.run(
                        GetWritingSentenceFeedbackApplication.Request(
                            user = user!!,
                            sentenceId = sentence!!.id,
                            submittedContent = "test",
                        ),
                    )

                    // then
                    wordService.getSentenceFeedbackBy(sentence!!.id, "test") shouldNotBe null
                    verify(openAIService, times(1)).fetchWritingSentenceFeedback(any(), any(), any(), any())
                }

                it("should create userSentenceFeedback even if sentenceFeedback was already attempted") {
                    // given
                    mockOpenAIRequest()
                    val userSentence = wordFactory.createUserSentence(user!!.id!!, wordDefinition!!.id, sentence!!.id)
                    val sentenceFeedback = wordFactory.createSentenceFeedback(sentenceId = sentence!!.id)

                    // when
                    getWritingSentenceFeedbackApplication.run(
                        GetWritingSentenceFeedbackApplication.Request(
                            user = user!!,
                            sentenceId = sentence!!.id,
                            submittedContent = sentenceFeedback.submittedContent,
                        ),
                    )

                    // then
                    wordService.getUserSentenceFeedbacks(userSentence.id).size shouldBe 1
                    verify(openAIService, times(0)).fetchWritingSentenceFeedback(any(), any(), any(), any())
                }

                it("should create userSentenceFeedback and sentenceFeedback if submitted content was not attempted") {
                    // given
                    mockOpenAIRequest()
                    val userSentence = wordFactory.createUserSentence(user!!.id!!, wordDefinition!!.id, sentence!!.id)
                    wordService.getSentenceFeedbackBy(sentence!!.id, "test") shouldBe null

                    // when
                    getWritingSentenceFeedbackApplication.run(
                        GetWritingSentenceFeedbackApplication.Request(
                            user = user!!,
                            sentenceId = sentence!!.id,
                            submittedContent = "test",
                        ),
                    )

                    // then
                    wordService.getUserSentenceFeedbacks(userSentence.id).size shouldBe 1
                    wordService.getSentenceFeedbackBy(sentence!!.id, "test") shouldNotBe null
                    verify(openAIService, times(1)).fetchWritingSentenceFeedback(any(), any(), any(), any())
                }
            }
        }
    })

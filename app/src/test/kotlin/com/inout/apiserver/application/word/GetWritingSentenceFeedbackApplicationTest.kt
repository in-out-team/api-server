package com.inout.apiserver.application.word

import com.inout.apiserver.base.enums.SentenceType
import com.inout.apiserver.base.service.FeedbackService
import com.inout.apiserver.domain.user.MongoUserFactory
import com.inout.apiserver.domain.word.MongoWordFactory
import com.inout.apiserver.domain.word.MongoWordService
import com.inout.apiserver.domain.word.WordWithDefinitions
import com.inout.apiserver.error.BadRequestException
import com.inout.apiserver.error.NotFoundException
import com.inout.apiserver.extension.cleanUp
import com.inout.apiserver.helper.InOutSpringBootTest
import com.inout.apiserver.infrastructure.mongo.user.MongoUser
import com.inout.apiserver.infrastructure.mongo.word.MongoSentence
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.bson.types.ObjectId
import org.mockito.kotlin.any
import org.mockito.kotlin.clearInvocations
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.data.mongodb.core.MongoTemplate

@InOutSpringBootTest
class GetWritingSentenceFeedbackApplicationTest(
    private val getWritingSentenceFeedbackApplication: GetWritingSentenceFeedbackApplication,
    // services
    @SpyBean
    private val feedbackService: FeedbackService,
    @SpyBean
    private val wordService: MongoWordService,
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

        describe("when sentence is not found") {
            it("should raise NotFoundException") {
                // given
                val sentenceId = ObjectId()
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
            var word: WordWithDefinitions?
            var wordDefinition: WordWithDefinitions.WordDefinition? = null
            var sentence: MongoSentence? = null

            beforeEach {
                word = wordFactory.createWord()
                wordDefinition = word!!.definitions.first()
                sentence = wordFactory.createSentence(wordDefinition!!.id!!, type = SentenceType.WRITING)
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
                                sentenceId = sentence!!.id!!,
                                submittedContent = submittedContent,
                            ),
                        )

                    // then
                    response.feedback shouldBe "Your answer is correct!"
                }
            }

            describe("providing incorrect answer") {
                fun mockOpenAIRequest() {
                    doReturn("feedback")
                        .whenever(feedbackService)
                        .fetchWritingSentenceFeedback(any(), any(), any(), any())
                }

                afterEach {
                    clearInvocations(feedbackService)
                }

                it("should return existing sentence feedback if submitted content was already attempted") {
                    // given
                    val existingSentenceFeedback = wordFactory.createSentenceFeedback(sentenceId = sentence!!.id!!)

                    // when
                    val response =
                        getWritingSentenceFeedbackApplication.run(
                            GetWritingSentenceFeedbackApplication.Request(
                                user = user!!,
                                sentenceId = sentence!!.id!!,
                                submittedContent = existingSentenceFeedback.submittedContent,
                            ),
                        )

                    // then
                    response.feedback shouldBe existingSentenceFeedback.feedback
                    verify(feedbackService, times(0)).fetchWritingSentenceFeedback(any(), any(), any(), any())
                    verify(wordService, times(0)).createSentenceFeedback(any(), any(), any())
                }

                it("should raise error if max attempt is reached") {
                    // given
                    val userSentence = wordFactory.createUserSentence(user!!.id!!, wordDefinition!!.id!!, sentence!!.id!!)
                    repeat(3) {
                        val sentenceFeedback =
                            wordFactory.createSentenceFeedback(
                                sentenceId = sentence!!.id!!,
                                submittedContent = "I read a book $it",
                                feedback = "feedback",
                            )
                        wordFactory.createUserSentenceFeedback(
                            userSentenceId = userSentence.id!!,
                            sentenceFeedbackId = sentenceFeedback.id!!,
                        )
                    }

                    // when
                    val exception =
                        shouldThrow<BadRequestException> {
                            getWritingSentenceFeedbackApplication.run(
                                GetWritingSentenceFeedbackApplication.Request(
                                    user = user!!,
                                    sentenceId = sentence!!.id!!,
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
                    wordService.getUserSentenceBy(user!!.id!!, sentence!!.id!!) shouldBe null

                    // when
                    getWritingSentenceFeedbackApplication.run(
                        GetWritingSentenceFeedbackApplication.Request(
                            user = user!!,
                            sentenceId = sentence!!.id!!,
                            submittedContent = "test",
                        ),
                    )

                    // then
                    wordService.getUserSentenceBy(user!!.id!!, sentence!!.id!!) shouldNotBe null
                }

                it("should create sentenceFeedback if submitted content was not attempted") {
                    // given
                    mockOpenAIRequest()
                    wordService.getSentenceFeedbackBy(sentence!!.id!!, "test") shouldBe null

                    // when
                    getWritingSentenceFeedbackApplication.run(
                        GetWritingSentenceFeedbackApplication.Request(
                            user = user!!,
                            sentenceId = sentence!!.id!!,
                            submittedContent = "test",
                        ),
                    )

                    // then
                    wordService.getSentenceFeedbackBy(sentence!!.id!!, "test") shouldNotBe null
                    verify(feedbackService, times(1)).fetchWritingSentenceFeedback(any(), any(), any(), any())
                }

                it("should create userSentenceFeedback even if sentenceFeedback was already attempted") {
                    // given
                    mockOpenAIRequest()
                    val userSentence = wordFactory.createUserSentence(user!!.id!!, wordDefinition!!.id!!, sentence!!.id!!)
                    val sentenceFeedback = wordFactory.createSentenceFeedback(sentenceId = sentence!!.id!!)

                    // when
                    getWritingSentenceFeedbackApplication.run(
                        GetWritingSentenceFeedbackApplication.Request(
                            user = user!!,
                            sentenceId = sentence!!.id!!,
                            submittedContent = sentenceFeedback.submittedContent,
                        ),
                    )

                    // then
                    wordService.getUserSentenceFeedbacks(userSentence.id!!).size shouldBe 1
                    verify(feedbackService, times(0)).fetchWritingSentenceFeedback(any(), any(), any(), any())
                }

                it("should create userSentenceFeedback and sentenceFeedback if submitted content was not attempted") {
                    // given
                    mockOpenAIRequest()
                    val userSentence = wordFactory.createUserSentence(user!!.id!!, wordDefinition!!.id!!, sentence!!.id!!)
                    wordService.getSentenceFeedbackBy(sentence!!.id!!, "test") shouldBe null

                    // when
                    getWritingSentenceFeedbackApplication.run(
                        GetWritingSentenceFeedbackApplication.Request(
                            user = user!!,
                            sentenceId = sentence!!.id!!,
                            submittedContent = "test",
                        ),
                    )

                    // then
                    wordService.getUserSentenceFeedbacks(userSentence.id!!).size shouldBe 1
                    wordService.getSentenceFeedbackBy(sentence!!.id!!, "test") shouldNotBe null
                    verify(feedbackService, times(1)).fetchWritingSentenceFeedback(any(), any(), any(), any())
                }
            }
        }
    })

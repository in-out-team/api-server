package com.inout.apiserver.base.util

import com.inout.apiserver.base.enums.LanguageType
import com.inout.apiserver.base.enums.SenderType
import com.inout.apiserver.base.service.FeedbackService
import com.inout.apiserver.base.service.openai.OpenAIFeedbackService
import com.inout.apiserver.domain.word.WordFactory
import com.inout.apiserver.domain.word.WordWithDefinitions
import com.inout.apiserver.extension.cleanUp
import com.inout.apiserver.helper.InOutSpringBootTest
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.ints.shouldBeGreaterThan
import org.springframework.data.mongodb.core.MongoTemplate

@InOutSpringBootTest
class OpenAIFeedbackServiceTest(
    private val subject: OpenAIFeedbackService,
    // factories
    private val wordFactory: WordFactory,
    // etc
    private val mongoTemplate: MongoTemplate,
) : DescribeSpec({
        afterEach {
            mongoTemplate.cleanUp()
        }

        xdescribe("fetchWritingSentenceFeedback") {
            it("should return feedback") {
                // given
                val originalContent = "I ate an apple"
                val submittedContent = "I eat an apple"
                val fromLanguageType = LanguageType.ENGLISH
                val toLanguageType = LanguageType.KOREAN

                // when
                val result =
                    subject.fetchWritingSentenceFeedback(
                        originalContent,
                        submittedContent,
                        fromLanguageType,
                        toLanguageType,
                    )

                // then
                result.length shouldBeGreaterThan 0
            }
        }

        xdescribe("fetchConversationFeedback") {
            var word: WordWithDefinitions? = null

            beforeEach {
                word = wordFactory.createWord()
            }

            it("should return system response.") {
                // when
                val response =
                    subject.fetchConversationFeedback(
                        fromLanguage = word!!.fromLanguage,
                        toLanguage = word!!.toLanguage,
                        wordName = word!!.name,
                        wordMeaning = word!!.definitions.first().meaning,
                        conversations =
                            listOf(
                                FeedbackService.Conversation(
                                    sender = SenderType.SYSTEM,
                                    message = "Let's talk about ${word!!.name}!",
                                ),
                                FeedbackService.Conversation(
                                    sender = SenderType.USER,
                                    message = "I don't know what ${word!!.name} means",
                                ),
                            ),
                    )

                // Then
                response.length shouldBeGreaterThan 0
            }
        }
    })

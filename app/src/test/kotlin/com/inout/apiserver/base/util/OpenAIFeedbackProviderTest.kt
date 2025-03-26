package com.inout.apiserver.base.util

import com.inout.apiserver.base.enums.LanguageType
import com.inout.apiserver.base.service.openai.OpenAIFeedbackService
import com.inout.apiserver.helper.InOutSpringBootTest
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.comparables.shouldBeGreaterThan

@InOutSpringBootTest
class OpenAIFeedbackProviderTest(
    private val subject: OpenAIFeedbackService,
) : DescribeSpec({
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
    })

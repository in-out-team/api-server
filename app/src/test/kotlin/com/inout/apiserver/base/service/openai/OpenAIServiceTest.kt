package com.inout.apiserver.base.service.openai

import com.inout.apiserver.extension.cleanUp
import com.inout.apiserver.helper.InOutSpringBootTest
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import org.springframework.jdbc.core.JdbcTemplate

@InOutSpringBootTest
class OpenAIServiceTest(
    private val openAIService: OpenAIService,
    // etc
    private val jdbcTemplate: JdbcTemplate,
) : DescribeSpec({
        afterEach {
            jdbcTemplate.cleanUp()
        }

        xdescribe("fetchWordDefinition") {
            it("should return OpenAIWordDefinitionResponse") {
                // Given
                val word = "book"
                val fromLanguage = "English"
                val toLanguage = "Korean"

                // When
                val response = openAIService.fetchWordDefinition(word, fromLanguage, toLanguage)

                // Then
                response.definitions.size shouldBeGreaterThan 0
            }
        }

        xdescribe("fetchWordDefinitionSentence") {
            it("should return OpenAIWordDefinitionSentenceResponse") {
                // Given
                val word = "book"
                val meaning = "예약하다"
                val fromLanguage = "English"
                val toLanguage = "Korean"

                // When
                val response = openAIService.fetchWordDefinitionSentence(word, meaning, fromLanguage, toLanguage)

                // Then
                response.sentences.size shouldBe 10
            }
        }
    })

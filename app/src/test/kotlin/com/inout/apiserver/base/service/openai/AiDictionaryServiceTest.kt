package com.inout.apiserver.base.service.openai

import com.inout.apiserver.base.enums.LanguageType
import com.inout.apiserver.extension.cleanUp
import com.inout.apiserver.helper.InOutSpringBootTest
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import org.springframework.jdbc.core.JdbcTemplate

@InOutSpringBootTest
class AiDictionaryServiceTest(
    private val subject: AiDictionaryService,
    // etc
    private val jdbcTemplate: JdbcTemplate,
) : DescribeSpec({
        afterEach {
            jdbcTemplate.cleanUp()
        }

        xdescribe("fetchWordDefinitions") {
            it("should return word definitions") {
                // Given
                val word = "spring"
                val fromLanguage = LanguageType.ENGLISH
                val toLanguage = LanguageType.KOREAN

                // When
                val response = subject.fetchWordDefinitions(word, fromLanguage, toLanguage)

                // Then
                response.size shouldBeGreaterThan 0
            }

            it("should return empty list when word is invalid") {
                // Given
                val word = "invalid_word"
                val fromLanguage = LanguageType.ENGLISH
                val toLanguage = LanguageType.KOREAN

                // When
                val response = subject.fetchWordDefinitions(word, fromLanguage, toLanguage)

                // Then
                response.size shouldBe 0
            }
        }
    })

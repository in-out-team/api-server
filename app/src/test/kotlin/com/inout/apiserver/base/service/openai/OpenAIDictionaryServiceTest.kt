package com.inout.apiserver.base.service.openai

import com.inout.apiserver.base.enums.LanguageType
import com.inout.apiserver.base.service.DictionaryService
import com.inout.apiserver.extension.cleanUp
import com.inout.apiserver.helper.InOutSpringBootTest
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import org.springframework.jdbc.core.JdbcTemplate

@InOutSpringBootTest
class OpenAIDictionaryServiceTest(
    private val subject: OpenAIDictionaryService,
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

        xdescribe("validateWordDefinition") {
            it("should return true when definition is valid") {
                // Given
                val word = "spring"
                val fromLanguage = LanguageType.ENGLISH
                val toLanguage = LanguageType.KOREAN
                val definition =
                    DictionaryService.Definition(
                        lexicalCategory = "noun",
                        definition = "봄",
                        preContext = "사계절 중 하나",
                    )

                // When
                val response = subject.validateWordDefinition(word, fromLanguage, toLanguage, definition)

                // Then
                response shouldBe true
            }

            it("should return false when definition is invalid") {
                // Given
                val word = "spring"
                val fromLanguage = LanguageType.ENGLISH
                val toLanguage = LanguageType.KOREAN
                val definition =
                    DictionaryService.Definition(
                        lexicalCategory = "noun",
                        definition = "가을",
                        preContext = "사계절 중 하나",
                    )

                // When
                val response = subject.validateWordDefinition(word, fromLanguage, toLanguage, definition)

                // Then
                response shouldBe false
            }
        }

        xdescribe("fetchWordDefinitionSentences") {
            it("should return sentences of given word's meaning") {
                // Given
                val word = "consider"
                val meaning = "고려하다"
                val fromLanguage = LanguageType.ENGLISH
                val toLanguage = LanguageType.KOREAN

                // When
                val response = subject.fetchWordDefinitionSentences(word, meaning, fromLanguage, toLanguage)

                // Then
                response.size shouldBe 10
            }
        }
    })

package com.inout.apiserver.base.service.openai

import com.inout.apiserver.extension.cleanUp
import com.inout.apiserver.helper.InOutSpringBootTest
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.mock.web.MockMultipartFile
import java.nio.file.Files
import java.nio.file.Paths
import java.util.regex.Pattern

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

        xdescribe("fetchWritingSentenceFeedback") {
            it("should throw exception when originalContent is the same as submittedContent") {
                // Given
                val originalContent = "I am a student."
                val submittedContent = "I am a student."
                val fromLanguage = "English"
                val toLanguage = "Korean"

                // When
                val exception =
                    shouldThrow<IllegalArgumentException> {
                        openAIService.fetchWritingSentenceFeedback(
                            originalContent,
                            submittedContent,
                            fromLanguage,
                            toLanguage,
                        )
                    }

                // Then
                exception.message shouldBe "originalContent and submittedContent must be different"
            }

            it("should return OpenAIWritingSentenceFeedbackResponse") {
                // Given
                val originalContent = "He is not coming back"
                val submittedContent = "He is coming not back"
                val fromLanguage = "English"
                val toLanguage = "Korean"

                // When
                val response =
                    openAIService.fetchWritingSentenceFeedback(originalContent, submittedContent, fromLanguage, toLanguage)

                // Then
                response.feedback.length shouldBeGreaterThan 10
            }
        }

        xdescribe("fetchTextFromSpeech") {
            it("should return OpenAITextFromSpeechResponse") {
                // Given
                val path = Paths.get("src/test/resources/audio/test.mp3")
                val audioContent = Files.readAllBytes(path)
                val mockMultipartFile = MockMultipartFile("file", "test.mp3", "audio/mpeg", audioContent)
                val language = "English"

                // When
                val result = openAIService.fetchTextFromSpeech(mockMultipartFile, language)

                // Then
                val regex = "^hello world,? this is phil+ip choi\\.?$"
                val pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE)
                val matcher = pattern.matcher(result)

                matcher.find() shouldBe true
            }
        }
    })

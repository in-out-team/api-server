package com.inout.apiserver.base.service.openai

import com.inout.apiserver.base.enums.SenderType
import com.inout.apiserver.domain.user.UserFactory
import com.inout.apiserver.domain.word.WordFactory
import com.inout.apiserver.extension.cleanUp
import com.inout.apiserver.helper.InOutSpringBootTest
import com.inout.apiserver.infrastructure.db.user.User
import com.inout.apiserver.infrastructure.db.word.Word
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
    // factories
    private val userFactory: UserFactory,
    private val wordFactory: WordFactory,
    // etc
    private val jdbcTemplate: JdbcTemplate,
) : DescribeSpec({
        afterEach {
            jdbcTemplate.cleanUp()
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

        xdescribe("fetchConversation") {
            var user: User? = null
            var word: Word? = null
            var wordDefinitionId = 0L

            beforeEach {
                user = userFactory.createUser()
                word = wordFactory.createWord()
                wordDefinitionId = word!!.definitions.first().id!!
            }

            it("should return system response.") {
                // Given
                val conversation = wordFactory.createConversation(user!!.id!!, wordDefinitionId)

                // When
                val response =
                    openAIService.fetchConversation(
                        fromLanguage = "English",
                        toLanguage = "Korean",
                        wordName = word!!.name,
                        wordMeaning = word!!.definitions.first().meaning,
                        messages =
                            listOf(
                                SenderType.SYSTEM to "Let's talk about ${word!!.name}!",
                                SenderType.USER to "I don't know what ${word!!.name} means",
                            ),
                    )

                // Then
                response.length shouldBeGreaterThan 0
            }
        }
    })

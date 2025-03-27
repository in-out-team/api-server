package com.inout.apiserver.base.service.openai

import com.inout.apiserver.base.enums.LanguageType
import com.inout.apiserver.helper.InOutSpringBootTest
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.springframework.mock.web.MockMultipartFile
import java.nio.file.Files
import java.nio.file.Paths
import java.util.regex.Pattern

@InOutSpringBootTest
class OpenAIAudioServiceTest(
    private val subject: OpenAIAudioService,
) : DescribeSpec({
        xdescribe("fetchTextFromSpeech") {
            it("should return OpenAITextFromSpeechResponse") {
                // Given
                val path = Paths.get("src/test/resources/audio/test.mp3")
                val audioContent = Files.readAllBytes(path)
                val mockMultipartFile = MockMultipartFile("file", "test.mp3", "audio/mpeg", audioContent)
                val language = LanguageType.ENGLISH

                // When
                val result = subject.fetchTextFromSpeech(mockMultipartFile, language)

                // Then
                val regex = "^hello world,? this is phil+ip choi\\.?$"
                val pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE)
                val matcher = pattern.matcher(result)

                matcher.find() shouldBe true
            }
        }
    })

package com.inout.apiserver.base.service.openai

import com.inout.apiserver.helper.InOutSpringBootTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

@Disabled
@InOutSpringBootTest
class OpenAIServiceTest(
    private val openAIService: OpenAIService,
) {
    @Test
    fun `fetchWordDefinition - should return OpenAIWordDefinitionResponse`() {
        // Given
        val word = "book"
        val fromLanguage = "English"
        val toLanguage = "Korean"

        // When
        val response = openAIService.fetchWordDefinition(word, fromLanguage, toLanguage)

        // Then
        assertThat(response.definitions.size).isGreaterThan(0)
    }

    @Test
    fun `fetchWordDefinitionSentence - should return OpenAIWordDefinitionSentenceResponse`() {
        // Given
        val word = "book"
        val meaning = "예약하다"
        val fromLanguage = "English"
        val toLanguage = "Korean"

        // When
        val response = openAIService.fetchWordDefinitionSentence(word, meaning, fromLanguage, toLanguage)

        // Then
        assertThat(response.sentences.size).isEqualTo(10)
    }
}

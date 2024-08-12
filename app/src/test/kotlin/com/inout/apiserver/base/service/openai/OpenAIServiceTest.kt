package com.inout.apiserver.base.service.openai

import com.aallam.openai.api.chat.*
import com.aallam.openai.api.core.FinishReason
import com.aallam.openai.api.core.Usage
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.inout.apiserver.base.service.openai.provider.OpenAIRequestProvider
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class OpenAIServiceTest {
    private val openAIRequestProvider = mockk<OpenAIRequestProvider>()
    private val openai = mockk<OpenAI>()
    private val openAIService = OpenAIService(openAIRequestProvider, openai)

    @Test
    fun `fetchWordDefinition - should return OpenAIWordDefinitionResponse`() {
        // Given
        val word = "book"
        val fromLanguage = "English"
        val toLanguage = "Korean"
        val chatCompletionRequest = mockk<ChatCompletionRequest>()
        val response = successResponse()
        every { openAIRequestProvider.genWordInfoRequest(word, fromLanguage, toLanguage) } returns chatCompletionRequest
        coEvery { openai.chatCompletion(chatCompletionRequest) } returns response

        // When
        val openAIWordDefinitionResponse = openAIService.fetchWordDefinition(word, fromLanguage, toLanguage)

        // Then
        assertNotNull(openAIWordDefinitionResponse)
        assertEquals(1, openAIWordDefinitionResponse.definitions.size)
        assertEquals("noun", openAIWordDefinitionResponse.definitions[0].type)
        assertEquals("바나나", openAIWordDefinitionResponse.definitions[0].definition)
        assertEquals("과일로 먹는 식품", openAIWordDefinitionResponse.definitions[0].preContext)
    }

    // TODO: add failing test cases

    private fun successResponse(): ChatCompletion {
        return ChatCompletion(
            id = "chatcmpl-9YPHOqmrcrv86cQL79X7nMEy88xkC",
            created = 1717987354,
            model = ModelId("gpt-3.5-turbo-0125"),
            usage = Usage(
                promptTokens = 420,
                completionTokens = 92,
                totalTokens = 512
            ),
            choices = listOf(
                ChatChoice(
                    index = 0,
                    logprobs = null,
                    finishReason = FinishReason.Stop,
                    message = ChatMessage(
                        role = ChatRole.System,
                        messageContent = TextContent(
                            content = """
                                {
                                    "definitions": [
                                        {
                                            "type": "noun",
                                            "definition": "바나나",
                                            "preContext": "과일로 먹는 식품"
                                        }
                                    ]
                                }
                            """.trimIndent()
                        )
                    )
                )
            )
        )
    }
}
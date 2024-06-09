package com.inout.apiserver.base.client

import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatResponseFormat
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component

@Component
class OpenAIApiClient(
    openAIProperties: OpenAIProperties
) {
    // https://github.com/aallam/openai-kotlin/blob/main/guides/GettingStarted.md
    private val openai = OpenAI(
        token = openAIProperties.token,
        organization = openAIProperties.organization
    )

    fun fetchWordInfo(word: String, language: String = "English"): String {
        val chatCompletionRequest = ChatCompletionRequest(
            model = ModelId("gpt-3.5-turbo"),
            messages = listOf(
                ChatMessage(
                    role = ChatRole.System,
                    content = "you are a helpful assistant"
                ),
                ChatMessage(
                    role = ChatRole.User,
                    content = "What is the meaning of $word in $language?. return in json format with key meaning and value as the meaning of the word."
                )
            ),
            responseFormat = ChatResponseFormat.JsonObject
        )

        val response = runBlocking { openai.chatCompletion(chatCompletionRequest) }
        return response.choices.first().message.content ?: "No response"
    }
}

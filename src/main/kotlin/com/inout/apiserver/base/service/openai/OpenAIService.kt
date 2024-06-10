package com.inout.apiserver.base.service.openai

import com.aallam.openai.api.chat.TextContent
import com.aallam.openai.client.OpenAI
import com.inout.apiserver.base.service.openai.config.OpenAIProperties
import com.inout.apiserver.base.service.openai.dto.OpenAIWordDefinitionResponse
import com.inout.apiserver.base.service.openai.provider.OpenAIRequestProvider
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Service

@Service
class OpenAIService(
    openAIProperties: OpenAIProperties,
    private val openAIRequestProvider: OpenAIRequestProvider,
) {
    // https://github.com/aallam/openai-kotlin/blob/main/guides/GettingStarted.md
    private val openai = OpenAI(
        token = openAIProperties.token,
        organization = openAIProperties.organization
    )

    fun fetchWordDefinition(
        word: String,
        fromLanguage: String = "English",
        toLanguage: String = "Korean",
    ): OpenAIWordDefinitionResponse {
        val chatCompletionRequest = openAIRequestProvider.genWordInfoRequest(word, fromLanguage, toLanguage)
        val response = runBlocking { openai.chatCompletion(chatCompletionRequest) }

        /**
         * TODO: log response
         *   ex: token usage, caller info
         *
         * response format: ChatCompletion.class
         *   - docs: https://platform.openai.com/docs/api-reference/chat/object
         * {
         *   "id": "chatcmpl-9YPHOqmrcrv86cQL79X7nMEy88xkC",
         *   "created": 1717987354,
         *   "model": "gpt-3.5-turbo-0125",
         *   "usage": { // Usage.class
         *     "promptTokens": 420,
         *     "completionTokens": 92,
         *     "totalTokens": 512
         *   },
         *   "choices": [ // List<ChatChoice.class>
         *       {
         *         "index": 0,
         *         "longpobs": null,
         *         "finishReason": "stop", // stop | length | content_filter | tool_calls
         *         "message": {
         *           "role": "assistant",
         *           "messageContent": { // TextContent.class
         *             "content": { // Content.class
         *               {
         *                 "definitions": [
         *                   {
         *                     "type": "noun",
         *                     "definition": "바나나",
         *                     "preContext": "과일로 먹는 식품"
         *                   },
         *                   {
         *                     "type": "noun",
         *                     "definition": "바나나 나무",
         *                     "preContext": "열대지방에서 자라는 나무"
         *                   }
         *                 ]
         *               }
         *             }
         *           }
         *         }
         *       }
         *   ]
         * }
         */
        val chatMessageContent = response.choices.first().message.messageContent as TextContent

        return OpenAIWordDefinitionResponse.fromJson(chatMessageContent.content)
    }
}

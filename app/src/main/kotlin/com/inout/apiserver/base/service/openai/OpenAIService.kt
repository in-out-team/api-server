package com.inout.apiserver.base.service.openai

import com.aallam.openai.api.chat.TextContent
import com.aallam.openai.client.OpenAI
import com.inout.apiserver.base.enums.SenderType
import com.inout.apiserver.base.service.openai.provider.OpenAIRequestProvider
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Service

@Service
class OpenAIService(
    private val openAIRequestProvider: OpenAIRequestProvider,
    private val openai: OpenAI,
) {
    /**
     * @param messages: List<Pair<SenderType, String>>
     *   - SenderType: USER | SYSTEM
     *   - String: message content
     */
    fun fetchConversation(
        fromLanguage: String,
        toLanguage: String,
        wordName: String,
        wordMeaning: String,
        messages: List<Pair<SenderType, String>>,
    ): String {
        val conversationRequest =
            openAIRequestProvider.genConversationRequest(
                fromLanguage = fromLanguage,
                toLanguage = toLanguage,
                wordName = wordName,
                wordMeaning = wordMeaning,
                messages = messages,
            )
        val response = runBlocking { openai.chatCompletion(conversationRequest) }
        val chatMessageContent =
            response.choices
                .first()
                .message.messageContent as TextContent
        return chatMessageContent.content
    }
}

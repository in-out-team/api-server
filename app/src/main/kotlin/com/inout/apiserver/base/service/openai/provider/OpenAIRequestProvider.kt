package com.inout.apiserver.base.service.openai.provider

import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatResponseFormat
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.model.ModelId
import com.inout.apiserver.application.word.RespondToConversationApplication
import com.inout.apiserver.base.enums.SenderType
import org.springframework.stereotype.Component

@Component
class OpenAIRequestProvider {
    private val chatCompletionModel = ModelId("gpt-3.5-turbo")

    fun genConversationRequest(
        fromLanguage: String,
        toLanguage: String,
        wordName: String,
        wordMeaning: String,
        // first: SenderType, second: message content
        messages: List<Pair<SenderType, String>>,
    ): ChatCompletionRequest {
        val continueConversation =
            messages.count { it.first == SenderType.SYSTEM } < RespondToConversationApplication.MAX_SYSTEM_RESPONSES - 1
        val systemDefinition =
            ChatMessage(
                role = ChatRole.System,
                content =
                    """
                    You are an AI language conversation tutor helping users practice vocabulary in $fromLanguage.
                    ${
                        when (continueConversation) {
                            true ->
                                """
                                Your goal is to ensure the user naturally incorporates the target word into conversation while maintaining proper grammar and sentence structure.
                                
                                # Response Rules (Strict Adherence Mandatory):
                                1. Directly address the user's last message first before guiding them toward using the target word.
                                2. Encourage the user to use the target word if they haven't yet. Guide them with relevant, engaging questions.
                                3. Expand naturally on the conversation if the user has already used the word correctly.
                                4. Correct grammatical errors gently by providing a natural alternative without disrupting the flow.
                                5. Correct misuse of the word by offering a better usage example and a short explanation.
                                6. Redirect inappropriate or off-topic responses professionally while keeping the conversation engaging.
                                7. Keep responses concise (≤ 200 characters), engaging, and contextually relevant.
                                """.trimIndent()

                            false ->
                                """
                                Your goal is to conclude the conversation naturally. 
                                Acknowledge the user's last message in a meaningful way, ensuring proper grammar and sentence structure. 
                                Do not ask further questions or introduce new topics. Respond concisely (≤ 200 characters).
                                """.trimIndent()
                        }
                    }
                    """.trimIndent(),
            )

        val queryMessage =
            ChatMessage(
                role = ChatRole.User,
                content =
                    """
                    Target word: "$wordName"
                    Meaning of the word in $toLanguage: "$wordMeaning"
                    Continue: $continueConversation
                    Conversation history:
                    ${messages.joinToString(" --> ") { "${it.first}: ${it.second}" }}
                    """.trimIndent(),
            )

        return ChatCompletionRequest(
            model = chatCompletionModel,
            messages = listOf(systemDefinition, queryMessage),
            responseFormat = ChatResponseFormat.Text,
        )
    }
}

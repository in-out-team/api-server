package com.inout.apiserver.base.service.openai

import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatResponseFormat
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.chat.TextContent
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.inout.apiserver.base.enums.LanguageType
import com.inout.apiserver.base.service.FeedbackService
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component

@Component
class OpenAIFeedbackService(
    private val openAI: OpenAI,
) : FeedbackService {
    private val chatCompletionModel = ModelId("gpt-3.5-turbo")
    private val chatCompletionResponseFormat = ChatResponseFormat.JsonObject

    override fun fetchWritingSentenceFeedback(
        originalContent: String,
        submittedContent: String,
        fromLanguage: LanguageType,
        toLanguage: LanguageType,
    ): String {
        val systemDefinitionContent =
            """
You are a language learning assistant.
Your task is to provide feedback on a user's submitted answer for a sentence writing practice.
You will be given the following information:
- fromLanguage: The language the user is studying (this is the language the user is writing in)
- toLanguage: The language feedback MUST be provided in (this is the user's native language)
- originalContent: The correct sentence the user is trying to write
- submittedContent: The user's attempt at writing the sentence

Analyze the submittedContent and compare it to the originalContent.
Then, provide feedback according to these rules:
1. ALWAYS provide feedback in the toLanguage. This is crucial for the user's understanding.
2. If the submittedContent is incorrect, provide detailed feedback explaining:
- What specific errors were made
- Why these are considered errors
- Hints on how to correct the mistakes without revealing the exact correct answer
- Any grammar rules or vocabulary usage that need attention
3. Keep your feedback concise, not exceeding 200 words.
4. Use simple, clear language appropriate for language learners.
5. Be encouraging and supportive in your tone.
6. If the submittedContent is completely off or unrelated, gently guide the user back to the original task.
7. Must not include any inappropriate or offensive content.
8. NEVER directly provide the correct answer or any part of it in your feedback.
- Example: If originalContent is 'I ate an apple' and submittedContent is 'I eat an apple',
  DO NOT say 'ate' is the correct word or include 'I ate an apple' in the feedback.
- Instead, hint at the tense: 'Consider the time when this action occurred. Is it happening now or in the past?'

Remember, your goal is to help the user improve their language skills in a constructive and motivating manner without giving away the answer.

Response format must be JSON with the following key-value pair:
- feedback: feedback message for the user (in toLanguage)

Note that feedback MUST, MUST, MUST!!! be provided in the toLanguage.
For example, if fromLanguage is English and toLanguage is Korean, feedback must be in Korean.
- ex) original content: 'Today must be a good day.', submitted content: 'Must be a good day today.', fromLanguage: 'English', toLanguage: 'Korean'
  feedback: '{{한국말로 작성된 피드백, example: 단어의 순서를 확인해주세요. 'Today'는 문장의 시작에 사용하는 것이 자연스러운 표현입니다.}}'    
            """.trimIndent()
        val systemDefinition =
            ChatMessage(
                role = ChatRole.System,
                content = systemDefinitionContent,
            )

        val queryMessageContent =
            """
fromLanguage: ${fromLanguage.name}
toLanguage: ${toLanguage.name}
originalContent: $originalContent
submittedContent: $submittedContent
            """.trimIndent()
        val queryMessage =
            ChatMessage(
                role = ChatRole.User,
                content = queryMessageContent,
            )

        val chatCompletionRequest =
            ChatCompletionRequest(
                model = chatCompletionModel,
                messages = listOf(systemDefinition, queryMessage),
                responseFormat = chatCompletionResponseFormat,
            )
        val response = runBlocking { openAI.chatCompletion(chatCompletionRequest) }
        val chatMessageContent =
            response.choices
                .first()
                .message.messageContent as TextContent

        return try {
            FeedbackService.WritingSentenceFeedbackResponse.fromJson(chatMessageContent.content).feedback
        } catch (e: Exception) {
            "" // TODO: handle exception on API call failure
        }
    }
}

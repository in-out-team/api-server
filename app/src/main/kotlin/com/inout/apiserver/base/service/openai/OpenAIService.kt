package com.inout.apiserver.base.service.openai

import com.aallam.openai.api.chat.TextContent
import com.aallam.openai.client.OpenAI
import com.inout.apiserver.base.enums.AiVoiceType
import com.inout.apiserver.base.enums.SenderType
import com.inout.apiserver.base.service.openai.dto.OpenAIWordDefinitionSentenceResponse
import com.inout.apiserver.base.service.openai.dto.OpenAIWritingSentenceFeedbackResponse
import com.inout.apiserver.base.service.openai.provider.OpenAIRequestProvider
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class OpenAIService(
    private val openAIRequestProvider: OpenAIRequestProvider,
    private val openai: OpenAI,
) {
    fun fetchWordDefinitionSentence(
        word: String,
        meaning: String,
        fromLanguage: String = "English",
        toLanguage: String = "Korean",
    ): OpenAIWordDefinitionSentenceResponse {
        val chatCompletionRequest =
            openAIRequestProvider.genWordDefinitionSentenceInfoRequest(word, meaning, fromLanguage, toLanguage)
        val response = runBlocking { openai.chatCompletion(chatCompletionRequest) }
        val chatMessageContent =
            response.choices
                .first()
                .message.messageContent as TextContent
        return OpenAIWordDefinitionSentenceResponse.fromJson(chatMessageContent.content)
    }

    fun fetchWritingSentenceFeedback(
        originalContent: String,
        userSubmittedContent: String,
        fromLanguage: String = "English",
        toLanguage: String = "Korean",
    ): OpenAIWritingSentenceFeedbackResponse {
        val chatCompletionRequest =
            openAIRequestProvider.genWritingSentenceFeedbackRequest(
                originalContent,
                userSubmittedContent,
                fromLanguage,
                toLanguage,
            )
        val response = runBlocking { openai.chatCompletion(chatCompletionRequest) }
        val chatMessageContent =
            response.choices
                .first()
                .message.messageContent as TextContent
        return OpenAIWritingSentenceFeedbackResponse.fromJson(chatMessageContent.content)
    }

    fun fetchTextFromSpeech(
        file: MultipartFile,
        language: String = "English",
    ): String {
        val transcriptionRequest = openAIRequestProvider.genTranscriptionRequest(file, language)
        val transcription = runBlocking { openai.transcription(transcriptionRequest) }
        return transcription.text
    }

    fun fetchSpeechFromText(
        text: String,
        aiVoiceType: AiVoiceType = AiVoiceType.ALLOY,
    ): ByteArray {
        val speechRequest = openAIRequestProvider.genSpeechRequest(text, aiVoiceType)
        return runBlocking { openai.speech(speechRequest) }
    }

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

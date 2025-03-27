package com.inout.apiserver.base.service

import com.inout.apiserver.base.enums.LanguageType
import com.inout.apiserver.base.enums.SenderType
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

interface FeedbackService {
    fun fetchWritingSentenceFeedback(
        originalContent: String,
        submittedContent: String,
        fromLanguage: LanguageType,
        toLanguage: LanguageType,
    ): String

    fun fetchConversationFeedback(
        fromLanguage: LanguageType,
        toLanguage: LanguageType,
        wordName: String,
        wordMeaning: String,
        conversations: List<Conversation>,
    ): String

    @Serializable
    data class WritingSentenceFeedbackResponse(
        val feedback: String,
    ) {
        companion object {
            fun fromJson(jsonString: String): WritingSentenceFeedbackResponse {
                val json = Json { ignoreUnknownKeys = true }
                return json.decodeFromString<WritingSentenceFeedbackResponse>(jsonString)
            }
        }
    }

    data class Conversation(
        val sender: SenderType,
        val message: String,
    )
}

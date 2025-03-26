package com.inout.apiserver.base.service

import com.inout.apiserver.base.enums.LanguageType
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

interface FeedbackService {
    fun fetchWritingSentenceFeedback(
        originalContent: String,
        submittedContent: String,
        fromLanguage: LanguageType,
        toLanguage: LanguageType,
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
}

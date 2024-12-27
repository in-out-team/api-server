package com.inout.apiserver.base.service.openai.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class OpenAIWritingSentenceFeedbackResponse(
    val feedback: String,
) {
    companion object {
        /**
         * @throws MissingFieldException if the required fields are missing
         */
        fun fromJson(jsonString: String): OpenAIWritingSentenceFeedbackResponse {
            val json = Json { ignoreUnknownKeys = true }
            return json.decodeFromString<OpenAIWritingSentenceFeedbackResponse>(jsonString)
        }
    }
}

package com.inout.apiserver.base.service.openai.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class OpenAIWordDefinitionSentenceResponse(
    val sentences: List<Sentence>,
) {
    companion object {
        /**
         * @throws MissingFieldException if the required fields are missing
         */
        fun fromJson(jsonString: String): OpenAIWordDefinitionSentenceResponse {
            val json = Json { ignoreUnknownKeys = true }
            return json.decodeFromString<OpenAIWordDefinitionSentenceResponse>(jsonString)
        }
    }
}

@Serializable
data class Sentence(
    val content: String,
    val translation: String,
)

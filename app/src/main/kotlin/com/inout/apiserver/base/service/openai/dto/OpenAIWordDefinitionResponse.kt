package com.inout.apiserver.base.service.openai.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class OpenAIWordDefinitionResponse(
    val definitions: List<Definition>
) {
    companion object {
        /**
         * @throws MissingFieldException if the required fields are missing
         */
        fun fromJson(jsonString: String): OpenAIWordDefinitionResponse {
            val json = Json { ignoreUnknownKeys = true }
            return json.decodeFromString<OpenAIWordDefinitionResponse>(jsonString)
        }
    }
}

@Serializable
data class Definition(
    val type: String,
    val definition: String,
    val preContext: String
)

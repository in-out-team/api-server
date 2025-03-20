package com.inout.apiserver.base.service.openai

import com.inout.apiserver.base.enums.LanguageType
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

interface DictionaryService {
    fun fetchWordDefinitions(
        word: String,
        fromLanguage: LanguageType,
        toLanguage: LanguageType,
    ): List<Definition>

    suspend fun validateWordDefinition(
        word: String,
        fromLanguage: LanguageType,
        toLanguage: LanguageType,
        definition: Definition,
    ): Boolean

    @Serializable
    data class DefinitionsResponse(
        val definitions: List<Definition>,
    ) {
        companion object {
            fun fromJson(jsonString: String): DefinitionsResponse {
                val json = Json { ignoreUnknownKeys = true }
                return json.decodeFromString<DefinitionsResponse>(jsonString)
            }
        }
    }

    @Serializable
    data class Definition(
        val lexicalCategory: String,
        val definition: String,
        val preContext: String,
    )
}

package com.inout.apiserver.base.service

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

    fun fetchWordDefinitionSentences(
        word: String,
        meaning: String,
        fromLanguage: LanguageType,
        toLanguage: LanguageType,
    ): List<Sentence>

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

    @Serializable
    data class DefinitionSentencesResponse(
        val sentences: List<Sentence>,
    ) {
        companion object {
            fun fromJson(jsonString: String): DefinitionSentencesResponse {
                val json = Json { ignoreUnknownKeys = true }
                return json.decodeFromString<DefinitionSentencesResponse>(jsonString)
            }
        }
    }

    @Serializable
    data class Sentence(
        val content: String,
        val translation: String,
        val lexicalCategories: List<LexicalCategory>,
    )

    @Serializable
    data class LexicalCategory(
        val word: String,
        val lexicalCategory: String,
    )
}

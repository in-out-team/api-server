package com.inout.apiserver.base.service.openai

import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatResponseFormat
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.chat.TextContent
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.inout.apiserver.base.enums.LanguageType
import com.inout.apiserver.base.service.openai.dto.Definition
import com.inout.apiserver.base.service.openai.dto.OpenAIWordDefinitionResponse
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Service

@Service
class AiDictionaryService(
    private val openai: OpenAI,
) : DictionaryService {
    companion object {
        const val DETERMINISTIC_TEMPERATURE = 0.2 // set temperature to 0.2 for deterministic results
    }

    private val chatCompletionModel = ModelId("gpt-3.5-turbo")
    private val chatCompletionResponseFormat = ChatResponseFormat.JsonObject

    override fun fetchWordDefinitions(
        word: String,
        fromLanguage: LanguageType,
        toLanguage: LanguageType,
    ): List<Definition> {
        val content =
            """
You are a highly accurate language dictionary. Your job is to provide definitions for a given word in the specified target language.

Requirements:
- The word to define: $word
- Source language: $fromLanguage
- Target language: $toLanguage

Output Format:
Return a JSON array of definitions, each object containing:
  - type: One of ["noun", "verb", "adjective", "adverb", "pronoun", "preposition", "conjunction", "interjection", "article", "particle", "determiner"]
  - definition: The meaning of the word in $toLanguage
  - preContext: A brief phrase or context in $toLanguage providing hints about when or how the definition is commonly used. (e.g., for "mandate" meaning "강제하다, 요구하다", the preContext could be "법, 규정 등으로")

Guidelines:
- If the word is not valid or has no definition, return an empty definitions array.
- If the word has only one definition, return only that one.
- Do not make up definitions. Provide only real, valid meanings.
- Provide a maximum of 3 definitions.
- Each definition must have a distinct meaning. (For example, "book" as a noun meaning "a set of written pages" and as a verb meaning "to reserve something").

Example request:
Word: "book"
From language: "English"
To language: "Korean"

Example response:
{
  "definitions": [
    {"type": "noun","definition": "책","preContext": "문학, 교육 등에서"},
    {"type": "verb","definition": "예약하다","preContext": "호텔, 식당 등에서"}
  ]
}
            """.trimIndent()
        val systemDefinition =
            ChatMessage(
                role = ChatRole.System,
                content = content,
            )

        val chatCompletionRequest =
            ChatCompletionRequest(
                model = chatCompletionModel,
                messages = listOf(systemDefinition),
                responseFormat = chatCompletionResponseFormat,
                temperature = DETERMINISTIC_TEMPERATURE,
            )

        val response = runBlocking { openai.chatCompletion(chatCompletionRequest) }
        val textContent =
            response.choices
                .first()
                .message.messageContent as TextContent

        return try {
            OpenAIWordDefinitionResponse.fromJson(textContent.content).definitions
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun validateWordDefinition(
        word: String,
        fromLanguage: LanguageType,
        toLanguage: LanguageType,
        definition: Definition,
    ): Boolean {
        val validationPrompt =
            """
You are a language expert. Validate if the following definition is correct for the given word, source language, and target language.

Word: $word
Source language: $fromLanguage
Target language: $toLanguage

Definition to validate:
- type: ${definition.type}
 - lexical category of the word
- definition: ${definition.definition}
- preContext: ${definition.preContext}
 - a brief phrase or context in $toLanguage providing hints about when or how the definition is commonly used

Respond with "true" if the definition is correct and "false" if it is incorrect. No additional text.
            """.trimIndent()

        val systemDefinition =
            ChatMessage(
                role = ChatRole.System,
                content = validationPrompt,
            )

        val chatCompletionRequest =
            ChatCompletionRequest(
                model = ModelId("gpt-3.5-turbo"),
                messages = listOf(systemDefinition),
                responseFormat = ChatResponseFormat.Text,
                temperature = 0.2,
            )

        val response = openai.chatCompletion(chatCompletionRequest)
        val chatMessageContent =
            response.choices
                .first()
                .message.messageContent as TextContent

        return chatMessageContent.content.lowercase() == "true"
    }
}

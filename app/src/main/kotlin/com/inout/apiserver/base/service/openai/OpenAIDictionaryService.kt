package com.inout.apiserver.base.service.openai

import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatResponseFormat
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.chat.TextContent
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.inout.apiserver.base.enums.LanguageType
import com.inout.apiserver.base.service.DictionaryService
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Service

@Service
class OpenAIDictionaryService(
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
    ): List<DictionaryService.Definition> {
        val content =
            """
You are a highly accurate language dictionary. Your job is to provide definitions for a given word in the specified target language.

Requirements:
- The word to define: $word
- Source language: $fromLanguage
- Target language: $toLanguage

Output Format:
Return a JSON array of definitions, each object containing:
  - lexicalCategory: One of ["noun", "verb", "adjective", "adverb", "pronoun", "preposition", "conjunction", "interjection", "article", "particle", "determiner"]
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
    {"lexicalCategory": "noun","definition": "책","preContext": "문학, 교육 등에서"},
    {"lexicalCategory": "verb","definition": "예약하다","preContext": "호텔, 식당 등에서"}
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
            DictionaryService.DefinitionsResponse.fromJson(textContent.content).definitions
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun validateWordDefinition(
        word: String,
        fromLanguage: LanguageType,
        toLanguage: LanguageType,
        definition: DictionaryService.Definition,
    ): Boolean {
        val validationPrompt =
            """
You are a language expert. Validate if the following definition is correct for the given word, source language, and target language.

Word: $word
Source language: $fromLanguage
Target language: $toLanguage

Definition to validate:
- lexicalCategory: ${definition.lexicalCategory}
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
                temperature = DETERMINISTIC_TEMPERATURE,
            )

        val response = openai.chatCompletion(chatCompletionRequest)
        val chatMessageContent =
            response.choices
                .first()
                .message.messageContent as TextContent

        return chatMessageContent.content.lowercase() == "true"
    }

    override fun fetchWordDefinitionSentences(
        word: String,
        meaning: String,
        fromLanguage: LanguageType,
        toLanguage: LanguageType,
    ): List<DictionaryService.Sentence> {
        val content =
            """
You are an AI-powered dictionary generating example sentences for a given word and its specific meaning.

### Instructions:
1. Generate exactly 10 example sentences in $toLanguage using the given word with the specified meaning in $fromLanguage. No more, no less.
2. Ensure all sentences use the word with the given meaning only. 
 - ❌ Incorrect: If given "book" (책), do NOT include sentences like "I will book a hotel" (예약하다). 
 - ✅ Correct: Only sentences where "book" means "책".
3. Each sentence must:
 - Be unique in context (avoid repetition).
 - Be grammatically correct and natural.
 - Contain approximately 10 words.
 - Include both the example sentence and its translation.
 - Allow listing of plural forms if applicable.

### Response Format (JSON)
{
  "sentences": [
    {
      "content": "<example sentence using the given word>",
      "translation": "<translated sentence>",
      "lexicalCategories": [
        {"word": "<word1>","lexicalCategory": "<category>"},
        {"word": "<word2>","lexicalCategory": "<category>"},
        ...
      ]
    }
  ]
}

### Content:
- example sentence using the given word in $fromLanguage

### Translation:
- sentence in $toLanguage

### Lexical Categories:
- lexicalCategories: An array of objects, each representing a word from the "content" sentence.
 - Each object contains:
  - "word": The exact word as it appears in the sentence.
  - "lexicalCategory": One of: noun, verb, adjective, adverb, pronoun, preposition, conjunction, interjection, article.
 - Important:
  - The number of objects in "lexicalCategories" **must exactly match** the number of words in "content" (split by spaces).
  - Do NOT group words together—each word must have its own lexical category object.
            """.trimIndent()
        val systemDefinition =
            ChatMessage(
                role = ChatRole.System,
                content = content,
            )
        val queryMessage =
            ChatMessage(
                role = ChatRole.User,
                content = "word: $word, meaning: $meaning",
            )

        val chatCompletionRequest =
            ChatCompletionRequest(
                model = chatCompletionModel,
                messages = listOf(systemDefinition, queryMessage),
                responseFormat = chatCompletionResponseFormat,
                temperature = DETERMINISTIC_TEMPERATURE,
            )

        val response = runBlocking { openai.chatCompletion(chatCompletionRequest) }
        val textContent =
            response.choices
                .first()
                .message.messageContent as TextContent

        return try {
            DictionaryService.DefinitionSentencesResponse.fromJson(textContent.content).sentences
        } catch (e: Exception) {
            emptyList()
        }
    }
}

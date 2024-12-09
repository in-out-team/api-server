package com.inout.apiserver.base.service.openai.provider

import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatResponseFormat
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.model.ModelId
import org.springframework.stereotype.Component

@Component
class OpenAIRequestProvider {
    private val chatCompletionModel = ModelId("gpt-3.5-turbo")
    private val chatCompletionResponseFormat = ChatResponseFormat.JsonObject

    fun genWordInfoRequest(
        word: String,
        fromLanguage: String = "English",
        toLanguage: String = "Korean",
    ): ChatCompletionRequest {
        val systemDefinition =
            ChatMessage(
                role = ChatRole.System,
                content =
                    """
                    You are an $fromLanguage to $toLanguage dictionary listing the definitions of given word or phrase.
                    response format must be JSON with following key value pairs:
                    - definitions: list of definition objects
                    - definition object:
                     - type: lexical category in English
                       - lexical categories: noun, verb, adjective, adverb, pronoun, preposition, conjunction, interjection
                     - definition: meaning of the word in $toLanguage
                     - preContext: context which helps user understand the use case of provided definition
                    
                    For definition, provide matching $toLanguage word or phrase if found. Only when no matching word in
                    $toLanguage exists, provide a sentence.
                    - ex) for "book", respond with "책" instead of "종이를 여러 장 묶어 댄 물건"
                    
                    List up to 3 definition objects which must be distinct in meaning.
                    - ex) for "book", provide "책, 서적" in one definition object and "예약하다" in another instead of "책",
                      "서적", "예약하다".
                     
                    For preContext, give context which the provided definition is used in $toLanguage.
                    - ex) for "mandate", definition "강제하다, 요구하다" has preContext of "법, 규정 등으로" as it better explains
                      how the definition is used.
                    
                    Do not make up definitions.
                    - ex) for "banana", "바나나 튀김" is never used as banana's definition.
                    
                    Do not try to provide maximum definitions if you can't find enough.
                    - ex) for a given word "apple", you should not to provide 3 definition objects and try to make up
                      definitions that do not exist. Simple "사과" is enough.
                    
                    Respond 0 definition objects if provided $fromLanguage word or phrase does not exist in $toLanguage.
                    - ex) for "tteokbokki", since it is not an original English word, 0 definition objects should be returned.
                    """.trimIndent(),
            )
        val queryMessage =
            ChatMessage(
                role = ChatRole.User,
                content = word,
            )

        return ChatCompletionRequest(
            model = chatCompletionModel,
            messages = listOf(systemDefinition, queryMessage),
            responseFormat = chatCompletionResponseFormat,
        )
    }

    fun genWordDefinitionSentenceInfoRequest(
        word: String,
        meaning: String,
        fromLanguage: String = "English",
        toLanguage: String = "Korean",
    ): ChatCompletionRequest {
        val systemDefinition =
            ChatMessage(
                role = ChatRole.System,
                content =
                    """
                    You are an English to Korean dictionary which has list of example sentences .
                    Generate 10 example sentences in English using the given word and it's meaning in Korean.
                    
                    Each sentence should:
                    0. Must include the given word and provide sentence which uses the given meaning
                     - ex: If given word and meaning is "book" and "책", there must only be sentences using the meaning "책". Sentences using "book" with meaning "예약하다" is strictly prohibited
                    1. Be unique in context, avoiding repetitive themes
                    2. Contain approximately 10 words
                    3. Be grammatically correct
                    4. Include both the example sentence and its translation
                    5. Listing plural form (if noun) is allowed.
                    
                    Ensure the sentences cover a wide range of situations and uses of the word.
                    
                    Response format must be JSON with following key value pairs:
                    - sentences: list of sentence objects
                    - sentence object:
                     - content: example sentence of the given word in English
                     - translation: sentence in Korean
                     - lexicalCategories: lexical category object for each word of the content
                       - lexical category object:
                         - word: word in the sentence
                         - lexicalCategory: lexical category of the word
                         - possible lexical category: noun, verb, adjective, adverb, pronoun, preposition, conjunction, interjection, article
                       - ex) for "I'm reading a book", [{word: "I'm", lexicalCategory: "pronoun"}, {word: "reading", lexicalCategory: "verb"}, {word: "a", lexicalCategory: "article"}, {word: "book", lexicalCategory: "noun"}]
                    """.trimIndent(),
            )
        val queryMessage =
            ChatMessage(
                role = ChatRole.User,
                content = word,
            )

        return ChatCompletionRequest(
            model = chatCompletionModel,
            messages = listOf(systemDefinition, queryMessage),
            responseFormat = chatCompletionResponseFormat,
        )
    }
}

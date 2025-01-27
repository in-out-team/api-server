package com.inout.apiserver.base.service.openai.provider

import com.aallam.openai.api.audio.AudioResponseFormat
import com.aallam.openai.api.audio.TranscriptionRequest
import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatResponseFormat
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.file.FileSource
import com.aallam.openai.api.model.ModelId
import com.inout.apiserver.error.BadRequestException
import okio.source
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile

@Component
class OpenAIRequestProvider {
    private val chatCompletionModel = ModelId("gpt-3.5-turbo")
    private val chatCompletionResponseFormat = ChatResponseFormat.JsonObject
    private val transcriptionModel = ModelId("whisper-1")
    private val transcriptionResponseFormat = AudioResponseFormat.Text

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

    fun genWritingSentenceFeedbackRequest(
        originalContent: String,
        submittedContent: String,
        fromLanguage: String = "English",
        toLanguage: String = "Korean",
    ): ChatCompletionRequest {
        if (originalContent == submittedContent) {
            throw IllegalArgumentException("originalContent and submittedContent must be different")
        }

        val systemDefinition =
            ChatMessage(
                role = ChatRole.System,
                content =
                    """
                    You are a language learning assistant.
                    Your task is to provide feedback on a user's submitted answer for a sentence writing practice.
                    You will be given the following information:
                    - fromLanguage: The language the user is studying (this is the language the user is writing in)
                    - toLanguage: The language feedback MUST be provided in (this is the user's native language)
                    - originalContent: The correct sentence the user is trying to write
                    - submittedContent: The user's attempt at writing the sentence

                    Analyze the submittedContent and compare it to the originalContent.
                    Then, provide feedback according to these rules:
                    1. ALWAYS provide feedback in the toLanguage. This is crucial for the user's understanding.
                    2. If the submittedContent is incorrect, provide detailed feedback explaining:
                    - What specific errors were made
                    - Why these are considered errors
                    - Hints on how to correct the mistakes without revealing the exact correct answer
                    - Any grammar rules or vocabulary usage that need attention
                    3. Keep your feedback concise, not exceeding 200 words.
                    4. Use simple, clear language appropriate for language learners.
                    5. Be encouraging and supportive in your tone.
                    6. If the submittedContent is completely off or unrelated, gently guide the user back to the original task.
                    7. Must not include any inappropriate or offensive content.
                    8. NEVER directly provide the correct answer or any part of it in your feedback.
                    - Example: If originalContent is 'I ate an apple' and submittedContent is 'I eat an apple',
                      DO NOT say 'ate' is the correct word or include 'I ate an apple' in the feedback.
                    - Instead, hint at the tense: 'Consider the time when this action occurred. Is it happening now or in the past?'

                    Remember, your goal is to help the user improve their language skills in a constructive and motivating manner without giving away the answer.
                    
                    Response format must be JSON with the following key-value pair:
                    - feedback: feedback message for the user (in toLanguage)
                    
                    Note that feedback MUST, MUST, MUST!!! be provided in the toLanguage.
                    For example, if fromLanguage is English and toLanguage is Korean, feedback must be in Korean.
                    - ex) original content: 'Today must be a good day.', submitted content: 'Must be a good day today.', fromLanguage: 'English', toLanguage: 'Korean'
                      feedback: '{{한국말로 작성된 피드백, example: 단어의 순서를 확인해주세요. 'Today'는 문장의 시작에 사용하는 것이 자연스러운 표현입니다.}}'
                      
                    """.trimIndent(),
            )
        val queryMessage =
            ChatMessage(
                role = ChatRole.User,
                content =
                    """
                    fromLanguage: $fromLanguage
                    toLanguage: $toLanguage
                    originalContent: $originalContent
                    submittedContent: $submittedContent
                    """.trimIndent(),
            )

        return ChatCompletionRequest(
            model = chatCompletionModel,
            messages = listOf(systemDefinition, queryMessage),
            responseFormat = chatCompletionResponseFormat,
        )
    }

    fun genTranscriptionRequest(
        file: MultipartFile,
        language: String = "English",
    ): TranscriptionRequest {
        if (file.isEmpty) {
            throw BadRequestException(message = "Audio file cannot be empty", code = "OPENAI_001")
        }
        if (file.contentType != "audio/mpeg") {
            throw BadRequestException(message = "Content type must be audio/mpeg", code = "OPENAI_002")
        }

        return TranscriptionRequest(
            audio = FileSource(name = "request.mp3", source = file.inputStream.source()),
            model = transcriptionModel,
            // FIXME: temp, need a mapper which translates language to language code
            language = language.slice(0..1).lowercase(),
            responseFormat = transcriptionResponseFormat,
        )
    }
}

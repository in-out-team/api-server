package com.inout.apiserver.base.service.openai.dto

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.MissingFieldException
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class OpenAIWordDefinitionResponseTest {
    @Test
    fun `fromJson - should return OpenAIWordDefinitionResponse with correct definitions`() {
        // Given
        val jsonString = """
            {
                "definitions": [
                    {
                        "type": "noun",
                        "definition": "책",
                        "preContext": "종이를 여러장 묶어 댄 물건"
                    },
                    {
                        "type": "verb",
                        "definition": "예약하다",
                        "preContext": "숙소, 극장 등을"
                    }
                ]
            }
        """.trimIndent()

        // When
        val openAIWordDefinitionResponse = OpenAIWordDefinitionResponse.fromJson(jsonString)

        // Then
        assertEquals(2, openAIWordDefinitionResponse.definitions.size)
        assertEquals("noun", openAIWordDefinitionResponse.definitions[0].type)
        assertEquals("책", openAIWordDefinitionResponse.definitions[0].definition)
        assertEquals("종이를 여러장 묶어 댄 물건", openAIWordDefinitionResponse.definitions[0].preContext)
        assertEquals("verb", openAIWordDefinitionResponse.definitions[1].type)
        assertEquals("예약하다", openAIWordDefinitionResponse.definitions[1].definition)
        assertEquals("숙소, 극장 등을", openAIWordDefinitionResponse.definitions[1].preContext)
    }

    @Test
    fun `fromJson - should return OpenAIWordDefinitionResponse with empty definitions`() {
        // Given
        val jsonString = """
            {
                "definitions": []
            }
        """.trimIndent()

        // When
        val openAIWordDefinitionResponse = OpenAIWordDefinitionResponse.fromJson(jsonString)

        // Then
        assertEquals(0, openAIWordDefinitionResponse.definitions.size)
    }

    @OptIn(ExperimentalSerializationApi::class)
    @Test
    fun `fromJson - should raise error when json is invalid`() {
        // Given
        val jsonString1 = """
            {
                "definitions": [
                    {
                        "type": "noun",
                        "definition": "책"
                    }
                ]
            }
        """.trimIndent()
        val jsonString2 = """
            {
                "definitions": [
                    {
                        "type": "noun",
                        "preContext": "종이를 여러장 묶어 댄 물건"
                    }
                ]
            }
        """.trimIndent()
        val jsonString3 = """
            {
                "definitions": [
                    {
                        "definition": "책",
                        "preContext": "종이를 여러장 묶어 댄 물건"
                    }
                ]
            }
        """.trimIndent()

        // When & Then
        assertThrows<MissingFieldException> { OpenAIWordDefinitionResponse.fromJson(jsonString1) }
        assertThrows<MissingFieldException> { OpenAIWordDefinitionResponse.fromJson(jsonString2) }
        assertThrows<MissingFieldException> { OpenAIWordDefinitionResponse.fromJson(jsonString3) }
    }
}
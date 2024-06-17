package com.inout.apiserver.application.word

import com.inout.apiserver.base.enums.LanguageType
import com.inout.apiserver.base.service.openai.OpenAIService
import com.inout.apiserver.domain.word.Word
import com.inout.apiserver.domain.word.WordService
import com.inout.apiserver.error.ConflictException
import com.inout.apiserver.interfaces.web.v1.request.CreateWordRequest
import com.inout.apiserver.interfaces.web.v1.response.WordResponse
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class CreateWordApplicationTest {
    private val wordService = mockk<WordService>()
    private val openAIService = mockk<OpenAIService>()
    private val createWordApplication = CreateWordApplication(wordService, openAIService)
    private val now = LocalDateTime.now()

    @Test
    fun `run - should throw ConflictException if word already exists`() {
        // Given
        val request = CreateWordRequest(
            name = "name",
            fromLanguage = LanguageType.ENGLISH,
            toLanguage = LanguageType.KOREAN
        )
        val word = Word(
            id = 1L,
            name = request.name,
            fromLanguage = request.fromLanguage,
            toLanguage = request.toLanguage,
            definitions = emptyList(),
            createdAt = now,
            updatedAt = now
        )
        every {
            wordService.getWordByNameAndFromLanguageAndToLanguage(
                request.name,
                request.fromLanguage,
                request.toLanguage
            )
        } returns word

        // When
        val exception = assertThrows(ConflictException::class.java) {
            createWordApplication.run(request)
        }

        // Then
        assertEquals("Word already exists", exception.message)
        verify(exactly = 1) {
            wordService.getWordByNameAndFromLanguageAndToLanguage(
                request.name,
                request.fromLanguage,
                request.toLanguage
            )
        }
    }

    @Test
    fun `run - should return WordResponse`() {
        // Given
        val request = CreateWordRequest(
            name = "name",
            fromLanguage = LanguageType.ENGLISH,
            toLanguage = LanguageType.KOREAN
        )
        val word = Word(
            id = 1L,
            name = request.name,
            fromLanguage = request.fromLanguage,
            toLanguage = request.toLanguage,
            definitions = emptyList(),
            createdAt = now,
            updatedAt = now
        )
        every {
            wordService.getWordByNameAndFromLanguageAndToLanguage(
                request.name,
                request.fromLanguage,
                request.toLanguage
            )
        } returns null
        every { openAIService.fetchWordDefinition(any(), any(), any()) } returns mockk() {
            every { definitions } returns emptyList()
        }
        every { wordService.createWord(any()) } returns word

        // When
        val result: WordResponse = createWordApplication.run(request)

        // Then
        assertNotNull(result)
        verify(exactly = 1) {
            wordService.getWordByNameAndFromLanguageAndToLanguage(
                request.name,
                request.fromLanguage,
                request.toLanguage
            )
            openAIService.fetchWordDefinition(any(), any(), any())
            wordService.createWord(any())
        }
    }
}
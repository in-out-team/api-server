package com.inout.apiserver.application.word

import com.inout.apiserver.base.enums.LanguageType
import com.inout.apiserver.domain.word.Word
import com.inout.apiserver.domain.word.WordService
import com.inout.apiserver.error.NotFoundException
import com.inout.apiserver.interfaces.web.v1.request.ReadWordRequest
import com.inout.apiserver.interfaces.web.v1.response.WordResponse
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class ReadWordApplicationTest {
    private val wordService = mockk<WordService>()
    private val readWordApplication = ReadWordApplication(wordService)
    private val now = LocalDateTime.now()

    @Test
    fun `run - should throw NotFoundException if word not found`() {
        // Given
        val request = ReadWordRequest(
            name = "name",
            fromLanguage = LanguageType.ENGLISH,
            toLanguage = LanguageType.KOREAN
        )
        every {
            wordService.getWordByNameAndFromLanguageAndToLanguage(
                request.name,
                request.fromLanguage,
                request.toLanguage
            )
        } returns null

        // When
        val exception = assertThrows(NotFoundException::class.java) {
            readWordApplication.run(request)
        }

        // Then
        assertEquals("Word not found", exception.message)
        assertEquals("WORD_2", exception.code)
        verify(exactly = 1) {
            wordService.getWordByNameAndFromLanguageAndToLanguage(
                request.name,
                request.fromLanguage,
                request.toLanguage
            )
        }
    }

    @Test
    fun `run - should return WordResponse if word found`() {
        // Given
        val request = ReadWordRequest(
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
        val response: WordResponse = readWordApplication.run(request)

        // Then
        assertEquals(word.id, response.id)
        assertEquals(word.name, response.name)
        assertEquals(word.fromLanguage, response.fromLanguage)
        assertEquals(word.toLanguage, response.toLanguage)
        verify(exactly = 1) {
            wordService.getWordByNameAndFromLanguageAndToLanguage(
                request.name,
                request.fromLanguage,
                request.toLanguage
            )
        }
    }
}

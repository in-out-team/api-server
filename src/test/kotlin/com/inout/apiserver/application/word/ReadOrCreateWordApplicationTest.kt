package com.inout.apiserver.application.word

import com.inout.apiserver.domain.word.Word
import com.inout.apiserver.domain.word.WordService
import com.inout.apiserver.error.NotFoundException
import com.inout.apiserver.base.enums.LanguageType
import com.inout.apiserver.interfaces.web.v1.response.WordResponse
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime

class ReadOrCreateWordApplicationTest {
    private val wordService = mockk<WordService>()
    private val readOrCreateWordApplication = ReadOrCreateWordApplication(wordService)
    private val now = LocalDateTime.now()

    @Test
    fun `run - should raise NotFoundException if word is not found`() {
        // Given
        val name = "name"
        val language = LanguageType.ENGLISH
        every { wordService.getWordByNameAndLanguage(name, language) } returns null

        // When
        val exception = assertThrows<NotFoundException> {
            readOrCreateWordApplication.run(name, language)
        }

        // Then
        assertEquals("Word not found", exception.message)
        assertEquals("WORD_1", exception.code)
    }

    @Test
    fun `run - should return WordResponse if word is found`() {
        // Given
        val name = "name"
        val language = LanguageType.ENGLISH
        val word = Word(id = 1L, name = name, language = language, createdAt = now, updatedAt = now)
        every { wordService.getWordByNameAndLanguage(name, language) } returns word

        // When
        val result = readOrCreateWordApplication.run(name, language)

        // Then
        assertEquals(WordResponse.of(word), result)
    }
}
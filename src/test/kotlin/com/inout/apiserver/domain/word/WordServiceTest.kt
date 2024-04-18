package com.inout.apiserver.domain.word

import com.inout.apiserver.infrastructure.db.word.LanguageTypes
import com.inout.apiserver.infrastructure.db.word.WordEntity
import com.inout.apiserver.infrastructure.db.word.WordRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class WordServiceTest {
    private val wordRepository = mockk<WordRepository>()
    private val wordService = WordService(wordRepository)
    private val now = LocalDateTime.now()

    @Test
    fun `getWordByNameAndLanguage - should return Word if found`() {
        // Given
        val name = "name"
        val language = LanguageTypes.ENGLISH
        val word = Word(id = 1L, name = name, language = language, createdAt = now, updatedAt = now)
        every { wordRepository.findByNameAndLanguage(name, language) } returns word

        // When
        val result = wordService.getWordByNameAndLanguage(name, language)

        // Then
        assertEquals(word, result)
        verify(exactly = 1) { wordRepository.findByNameAndLanguage(name, language) }
    }

    @Test
    fun `getWordByNameAndLanguage - should return null if not found`() {
        // Given
        val name = "name"
        val language = LanguageTypes.ENGLISH
        every { wordRepository.findByNameAndLanguage(name, language) } returns null

        // When
        val result = wordService.getWordByNameAndLanguage(name, language)

        // Then
        assertNull(result)
        verify(exactly = 1) { wordRepository.findByNameAndLanguage(name, language) }
    }

    @Test
    fun `getWordById - should return Word if found`() {
        // Given
        val id = 1L
        val word = Word(id = id, name = "name", language = LanguageTypes.ENGLISH, createdAt = now, updatedAt = now)
        every { wordRepository.findById(id) } returns word

        // When
        val result = wordService.getWordById(id)

        // Then
        assertEquals(word, result)
        verify(exactly = 1) { wordRepository.findById(id) }
    }

    @Test
    fun `getWordById - should return null if not found`() {
        // Given
        val id = 1L
        every { wordRepository.findById(id) } returns null

        // When
        val result = wordService.getWordById(id)

        // Then
        assertNull(result)
        verify(exactly = 1) { wordRepository.findById(id) }
    }

    @Test
    fun `createWord - should return Word`() {
        // Given
        val wordCreateObject = WordCreateObject(name = "name", language = LanguageTypes.ENGLISH)
        val word = Word(id = 1L, name = wordCreateObject.name, language = wordCreateObject.language, createdAt = now, updatedAt = now)
        every { wordRepository.save(any()) } returns word

        // When
        val result = wordService.createWord(wordCreateObject)

        // Then
        assertEquals(word, result)
        verify(exactly = 1) { wordRepository.save(any()) }
    }
}
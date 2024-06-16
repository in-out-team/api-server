package com.inout.apiserver.domain.word

import com.inout.apiserver.base.enums.LanguageType
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
    fun `getWordByNameAndFromLanguageAndToLanguage - should return Word if found`() {
        // Given
        val name = "name"
        val fromLanguage = LanguageType.ENGLISH
        val toLanguage = LanguageType.KOREAN
        val word = Word(
            id = 1L,
            name = name,
            fromLanguage = fromLanguage,
            toLanguage = toLanguage,
            definitions = emptyList(),
            createdAt = now,
            updatedAt = now
        )
        every { wordRepository.findByNameAndFromLanguageAndToLanguage(name, fromLanguage, toLanguage) } returns word

        // When
        val result = wordService.getWordByNameAndFromLanguageAndToLanguage(name, fromLanguage, toLanguage)

        // Then
        assertEquals(word, result)
        verify(exactly = 1) { wordRepository.findByNameAndFromLanguageAndToLanguage(name, fromLanguage, toLanguage) }
    }

    @Test
    fun `getWordByNameAndFromLanguageAndToLanguage - should return null if not found`() {
        // Given
        val name = "name"
        val fromLanguage = LanguageType.ENGLISH
        val toLanguage = LanguageType.KOREAN
        every { wordRepository.findByNameAndFromLanguageAndToLanguage(name, fromLanguage, toLanguage) } returns null

        // When
        val result = wordService.getWordByNameAndFromLanguageAndToLanguage(name, fromLanguage, toLanguage)

        // Then
        assertNull(result)
        verify(exactly = 1) { wordRepository.findByNameAndFromLanguageAndToLanguage(name, fromLanguage, toLanguage) }
    }

    @Test
    fun `getWordById - should return Word if found`() {
        // Given
        val id = 1L
        val word = Word(
            id = id,
            name = "name",
            fromLanguage = LanguageType.ENGLISH,
            toLanguage = LanguageType.KOREAN,
            definitions = emptyList(),
            createdAt = now,
            updatedAt = now
        )
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
        val wordCreateObject =
            WordCreateObject(
                name = "name",
                fromLanguage = LanguageType.ENGLISH,
                toLanguage = LanguageType.KOREAN,
                definitions = emptyList()
            )
        val word = Word(
            id = 1L,
            name = wordCreateObject.name,
            fromLanguage = wordCreateObject.fromLanguage,
            toLanguage = wordCreateObject.toLanguage,
            definitions = emptyList(),
            createdAt = now,
            updatedAt = now
        )
        every { wordRepository.save(any()) } returns word

        // When
        val result = wordService.createWord(wordCreateObject)

        // Then
        assertEquals(word, result)
        verify(exactly = 1) { wordRepository.save(any()) }
    }
}
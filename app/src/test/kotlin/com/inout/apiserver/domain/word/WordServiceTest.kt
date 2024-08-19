package com.inout.apiserver.domain.word

import com.inout.apiserver.base.enums.LanguageType
import com.inout.apiserver.base.enums.LexicalCategoryType
import com.inout.apiserver.error.ConflictException
import com.inout.apiserver.infrastructure.db.word.WordRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import java.time.LocalDateTime

class WordServiceTest {
    private val wordRepository = mockk<WordRepository>()
    private val wordService = WordService(wordRepository)
    private val now = LocalDateTime.now()

    private fun wordsList() = listOf(
        Word(
            id = 1L,
            name = "book",
            fromLanguage = LanguageType.ENGLISH,
            toLanguage = LanguageType.KOREAN,
            definitions = listOf(
                WordDefinition(
                    id = 1L,
                    lexicalCategory = LexicalCategoryType.NOUN,
                    meaning = "책",
                    preContext = "정보를 얻거나 즐거움을 얻기 위해 읽는 인쇄물",
                ),
                WordDefinition(
                    id = 2L,
                    lexicalCategory = LexicalCategoryType.VERB,
                    meaning = "예약하다",
                    preContext = "특정한 날짜나 시간에 무엇을 하기 위해 미리 자리를 확보하다",
                )
            ),
            createdAt = now,
            updatedAt = now
        ),
        Word(
            id = 2L,
            name = "booked",
            fromLanguage = LanguageType.ENGLISH,
            toLanguage = LanguageType.KOREAN,
            definitions = listOf(
                WordDefinition(
                    id = 3L,
                    lexicalCategory = LexicalCategoryType.ADJECTIVE,
                    meaning = "예약된",
                    preContext = "미리 자리를 확보한",
                ),
                WordDefinition(
                    id = 4L,
                    lexicalCategory = LexicalCategoryType.VERB,
                    meaning = "예약하다",
                    preContext = "특정한 날짜나 시간에 무엇을 하기 위해 미리 자리를 확보하다",
                )
            ),
            createdAt = now,
            updatedAt = now
        )
    )

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
    fun `createWord - should raise ConflictException if word already exists`() {
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
        every { wordService.getWordByNameAndFromLanguageAndToLanguage(any(), any(), any()) } returns word

        // When, Then
        assertThrows(ConflictException::class.java) {
            wordService.createWord(wordCreateObject)
        }
        verify(exactly = 1) { wordService.getWordByNameAndFromLanguageAndToLanguage(any(), any(), any()) }
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
        every { wordService.getWordByNameAndFromLanguageAndToLanguage(any(), any(), any()) } returns null
        every { wordRepository.save(any()) } returns word

        // When
        val result = wordService.createWord(wordCreateObject)

        // Then
        assertEquals(word, result)
        verify(exactly = 1) { wordRepository.save(any()) }
    }

    @Test
    fun `getWordsWithDefinitions - should return Page of Words`() {
        // Given
        val fromLanguage = LanguageType.ENGLISH
        val toLanguage = LanguageType.KOREAN
        val prefix = "book"
        val lexicalCategoryType = null
        val pageable = PageRequest.of(0, 1)
        val words = wordsList()
        every {
            wordRepository.findWordsWithDefinitions(
                fromLanguage,
                toLanguage,
                prefix,
                lexicalCategoryType,
                pageable
            )
        } returns PageImpl(listOf(words.first()), pageable, words.size.toLong())

        // When
        val result = wordService.getWordsWithDefinitions(fromLanguage, toLanguage, prefix, lexicalCategoryType, pageable)

        // Then
        assertEquals(words.first(), result.content.first())
        assertEquals(words.size.toLong(), result.totalElements)
        verify(exactly = 1) {
            wordRepository.findWordsWithDefinitions(
                fromLanguage,
                toLanguage,
                prefix,
                lexicalCategoryType,
                pageable
            )
        }
    }
}
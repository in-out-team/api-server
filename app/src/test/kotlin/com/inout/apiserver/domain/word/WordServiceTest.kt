package com.inout.apiserver.domain.word

import com.inout.apiserver.base.enums.LanguageType
import com.inout.apiserver.base.enums.LexicalCategoryType
import com.inout.apiserver.error.ConflictException
import com.inout.apiserver.error.NotFoundException
import com.inout.apiserver.infrastructure.db.word.WordRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.time.Instant

class WordServiceTest {
    private val wordRepository = mockk<WordRepository>()
    private val wordService = WordService(wordRepository)
    private val now = Instant.now()

    private fun wordsList() =
        listOf(
            Word(
                id = 1L,
                name = "book",
                fromLanguage = LanguageType.ENGLISH,
                toLanguage = LanguageType.KOREAN,
                definitions =
                    listOf(
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
                        ),
                    ),
                createdAt = now,
                updatedAt = now,
            ),
            Word(
                id = 2L,
                name = "booked",
                fromLanguage = LanguageType.ENGLISH,
                toLanguage = LanguageType.KOREAN,
                definitions =
                    listOf(
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
                        ),
                    ),
                createdAt = now,
                updatedAt = now,
            ),
        )

    @Nested
    inner class GetWordByNameAndFromLanguageAndToLanguage {
        @Test
        fun `should return Word if found`() {
            // Given
            val name = "name"
            val fromLanguage = LanguageType.ENGLISH
            val toLanguage = LanguageType.KOREAN
            val word =
                Word(
                    id = 1L,
                    name = name,
                    fromLanguage = fromLanguage,
                    toLanguage = toLanguage,
                    definitions = emptyList(),
                    createdAt = now,
                    updatedAt = now,
                )
            every { wordRepository.findByNameAndFromLanguageAndToLanguage(name, fromLanguage, toLanguage) } returns word

            // When
            val sut = wordService.getWordByNameAndFromLanguageAndToLanguage(name, fromLanguage, toLanguage)

            // Then
            assertEquals(word, sut)
            verify(exactly = 1) {
                wordRepository.findByNameAndFromLanguageAndToLanguage(
                    name,
                    fromLanguage,
                    toLanguage,
                )
            }
        }

        @Test
        fun `should return null if not found`() {
            // Given
            val name = "name"
            val fromLanguage = LanguageType.ENGLISH
            val toLanguage = LanguageType.KOREAN
            every { wordRepository.findByNameAndFromLanguageAndToLanguage(name, fromLanguage, toLanguage) } returns null

            // When
            val sut = wordService.getWordByNameAndFromLanguageAndToLanguage(name, fromLanguage, toLanguage)

            // Then
            assertNull(sut)
            verify(exactly = 1) {
                wordRepository.findByNameAndFromLanguageAndToLanguage(
                    name,
                    fromLanguage,
                    toLanguage,
                )
            }
        }
    }

    @Nested
    inner class GetWordById {
        @Test
        fun `should return Word if found`() {
            // Given
            val id = 1L
            val word =
                Word(
                    id = id,
                    name = "name",
                    fromLanguage = LanguageType.ENGLISH,
                    toLanguage = LanguageType.KOREAN,
                    definitions = emptyList(),
                    createdAt = now,
                    updatedAt = now,
                )
            every { wordRepository.findById(id) } returns word

            // When
            val sut = wordService.getWordById(id)

            // Then
            assertEquals(word, sut)
            verify(exactly = 1) { wordRepository.findById(id) }
        }

        @Test
        fun `should return null if not found`() {
            // Given
            val id = 1L
            every { wordRepository.findById(id) } returns null

            // When
            val sut = wordService.getWordById(id)

            // Then
            assertNull(sut)
            verify(exactly = 1) { wordRepository.findById(id) }
        }
    }

    @Nested
    inner class CreateWord {
        @Test
        fun `should raise ConflictException if word already exists`() {
            // Given
            val wordCreateObject =
                WordCreateObject(
                    name = "name",
                    fromLanguage = LanguageType.ENGLISH,
                    toLanguage = LanguageType.KOREAN,
                    definitions = emptyList(),
                )
            val word =
                Word(
                    id = 1L,
                    name = wordCreateObject.name,
                    fromLanguage = wordCreateObject.fromLanguage,
                    toLanguage = wordCreateObject.toLanguage,
                    definitions = emptyList(),
                    createdAt = now,
                    updatedAt = now,
                )
            every { wordService.getWordByNameAndFromLanguageAndToLanguage(any(), any(), any()) } returns word

            // When, Then
            assertThrows(ConflictException::class.java) {
                wordService.createWord(wordCreateObject)
            }
            verify(exactly = 1) { wordService.getWordByNameAndFromLanguageAndToLanguage(any(), any(), any()) }
        }

        @Test
        fun `should return Word`() {
            // Given
            val wordCreateObject =
                WordCreateObject(
                    name = "name",
                    fromLanguage = LanguageType.ENGLISH,
                    toLanguage = LanguageType.KOREAN,
                    definitions = emptyList(),
                )
            val word =
                Word(
                    id = 1L,
                    name = wordCreateObject.name,
                    fromLanguage = wordCreateObject.fromLanguage,
                    toLanguage = wordCreateObject.toLanguage,
                    definitions = emptyList(),
                    createdAt = now,
                    updatedAt = now,
                )
            every { wordService.getWordByNameAndFromLanguageAndToLanguage(any(), any(), any()) } returns null
            every { wordRepository.save(any()) } returns word

            // When
            val sut = wordService.createWord(wordCreateObject)

            // Then
            assertEquals(word, sut)
            verify(exactly = 1) { wordRepository.save(any()) }
        }
    }

    @Nested
    inner class GetWordsWithDefinitions {
        @Test
        fun `should return Page of Words`() {
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
                    pageable,
                )
            } returns PageImpl(listOf(words.first()), pageable, words.size.toLong())

            // When
            val sut =
                wordService.getWordsWithDefinitions(fromLanguage, toLanguage, prefix, lexicalCategoryType, pageable)

            // Then
            assertEquals(words.first(), sut.content.first())
            assertEquals(words.size.toLong(), sut.totalElements)
            verify(exactly = 1) {
                wordRepository.findWordsWithDefinitions(
                    fromLanguage,
                    toLanguage,
                    prefix,
                    lexicalCategoryType,
                    pageable,
                )
            }
        }
    }

    @Nested
    inner class GetWordByWordDefinitionId {
        @Test
        fun `should raise NotFoundException if not word with given wordDefinitionId does not exist`() {
            // Given
            val wordDefinitionId = 1L
            every { wordRepository.findByWordDefinitionId(wordDefinitionId) } returns null

            // When, Then
            assertThrows(NotFoundException::class.java) {
                wordService.getWordByWordDefinitionId(wordDefinitionId)
            }
            verify(exactly = 1) { wordRepository.findByWordDefinitionId(wordDefinitionId) }
        }

        @Test
        fun `should return Word`() {
            // Given
            val wordDefinitionId = 1L
            val word = wordsList().first()
            every { wordRepository.findByWordDefinitionId(wordDefinitionId) } returns word

            // When
            val sut = wordService.getWordByWordDefinitionId(wordDefinitionId)

            // Then
            assertEquals(word.copy(definitions = word.definitions.filter { it.id == wordDefinitionId }), sut)
            verify(exactly = 1) { wordRepository.findByWordDefinitionId(wordDefinitionId) }
        }
    }

    @Nested
    inner class GetWordsByWordDefinitionIds {
        @Test
        fun `should return List of Words`() {
            // Given
            val words = wordsList()
            val wordDefinitionIds = words.first().definitions.map { it.id } + words.last().definitions.first().id
            every { wordRepository.findAllByWordDefinitionIds(wordDefinitionIds) } returns words

            // When
            val sut = wordService.getWordsByWordDefinitionIds(wordDefinitionIds)

            // Then
            assertEquals(2, sut.size)
            val firstWord = sut.first()
            assertEquals(words.first().definitions.map { it.id }, firstWord.definitions.map { it.id })
            val lastWord = sut.last()
            assertEquals(1, lastWord.definitions.size)
            assertEquals(words.last().definitions.first().id, lastWord.definitions.first().id)
        }
    }
}

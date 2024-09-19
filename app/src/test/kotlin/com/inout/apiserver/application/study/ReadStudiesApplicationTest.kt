package com.inout.apiserver.application.study

import com.inout.apiserver.base.enums.LanguageType
import com.inout.apiserver.base.enums.LexicalCategoryType
import com.inout.apiserver.domain.study.Study
import com.inout.apiserver.domain.study.StudyService
import com.inout.apiserver.domain.word.Word
import com.inout.apiserver.domain.word.WordDefinition
import com.inout.apiserver.domain.word.WordService
import com.inout.apiserver.error.InternalServerErrorException
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import java.time.Instant

class ReadStudiesApplicationTest {
    private val studyService = mockk<StudyService>()
    private val wordService = mockk<WordService>()
    private val readStudiesApplication = ReadStudiesApplication(studyService, wordService)
    private val now = Instant.now()

    private fun createWords(count: Int): List<Word> {
        return (1..count).map { i ->
            Word(
                id = i.toLong(),
                name = "name$i",
                fromLanguage = LanguageType.ENGLISH,
                toLanguage = LanguageType.KOREAN,
                definitions =
                    listOf(
                        WordDefinition(
                            id = i.toLong(),
                            lexicalCategory = LexicalCategoryType.NOUN,
                            meaning = "meaning$i",
                            preContext = "preContext$i",
                        ),
                    ),
                createdAt = now,
                updatedAt = now,
            )
        }
    }

    private fun createStudies(
        count: Int,
        userId: Long,
    ): List<Study> {
        return (1..count).map { i ->
            Study(
                id = i.toLong(),
                userId = userId,
                wordDefinitionId = i.toLong(),
                createdAt = now,
                updatedAt = now,
            )
        }
    }

    @Test
    fun `run - should raise error when study with no word exists`() {
        // Given
        val userId = 1L
        val pageable = mockk<Pageable>()
        val words = createWords(1)
        val studies = createStudies(2, userId)
        val wordDefinitionIds = studies.map { it.wordDefinitionId }

        every { studyService.getAllByUserId(userId, pageable) } returns PageImpl(studies)
        every { wordService.getWordsByWordDefinitionIds(wordDefinitionIds) } returns words

        // When
        val sut =
            assertThrows(InternalServerErrorException::class.java) {
                readStudiesApplication.run(userId, pageable)
            }

        // Then
        assertEquals("Data Integrity Error", sut.message)
        assertEquals("STUDY_2", sut.code)
    }

    @Test
    fun `run - should return studies`() {
        // Given
        val userId = 1L
        val pageable = mockk<Pageable>()
        val words = createWords(2)
        val studies = createStudies(2, userId)
        val wordDefinitionIds = studies.map { it.wordDefinitionId }

        every { studyService.getAllByUserId(userId, pageable) } returns PageImpl(studies)
        every { wordService.getWordsByWordDefinitionIds(wordDefinitionIds) } returns words

        // When
        val sut = readStudiesApplication.run(userId, pageable)

        // Then
        assertEquals(2, sut.first)
        assertEquals(2, sut.second.size)
        assertEquals(studies[0], sut.second[0].study)
        assertEquals(words[0], sut.second[0].word)
        assertEquals(studies[1], sut.second[1].study)
        assertEquals(words[1], sut.second[1].word)
    }
}

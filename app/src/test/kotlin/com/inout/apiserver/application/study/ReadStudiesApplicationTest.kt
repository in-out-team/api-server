package com.inout.apiserver.application.study

import com.inout.apiserver.base.alias.UserId
import com.inout.apiserver.base.enums.FsrsCardState
import com.inout.apiserver.base.enums.LanguageType
import com.inout.apiserver.base.enums.LexicalCategoryType
import com.inout.apiserver.domain.study.StudyService
import com.inout.apiserver.domain.word.WordService
import com.inout.apiserver.error.InternalServerErrorException
import com.inout.apiserver.infrastructure.db.study.Study
import com.inout.apiserver.infrastructure.db.word.Word
import com.inout.apiserver.infrastructure.db.word.WordDefinition
import com.inout.fsrs.model.Card
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable

class ReadStudiesApplicationTest {
    private val studyService = mockk<StudyService>()
    private val wordService = mockk<WordService>()
    private val readStudiesApplication = ReadStudiesApplication(studyService, wordService)

    private fun createWords(count: Int): List<Word> =
        (1..count).map { i ->
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
            )
        }

    private fun createStudies(
        count: Int,
        userId: UserId,
    ): List<Study> {
        val fsrsCard = Card.createEmptyCard()
        return (1..count).map { i ->
            Study(
                id = i.toLong(),
                userId = userId,
                wordDefinitionId = i.toLong(),
                state = FsrsCardState.of(fsrsCard.state),
                due = fsrsCard.due,
                stability = fsrsCard.stability,
                difficulty = fsrsCard.difficulty,
                elapsedDays = fsrsCard.elapsedDays,
                scheduledDays = fsrsCard.scheduledDays,
                reps = fsrsCard.reps,
                lapses = fsrsCard.lapses,
                lastReview = fsrsCard.lastReview,
                reviewLogs = emptyList(),
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
                readStudiesApplication.run(
                    ReadStudiesApplication.Request(
                        userId = userId,
                        pageable = pageable,
                    ),
                )
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
        val sut =
            readStudiesApplication.run(
                ReadStudiesApplication.Request(
                    userId = userId,
                    pageable = pageable,
                ),
            )

        // Then
        assertEquals(2, sut.totalCount)
        assertEquals(2, sut.studies.size)
        assertEquals(studies[0], sut.studies[0].study)
        assertEquals(words[0], sut.studies[0].word)
        assertEquals(studies[1], sut.studies[1].study)
        assertEquals(words[1], sut.studies[1].word)
    }
}

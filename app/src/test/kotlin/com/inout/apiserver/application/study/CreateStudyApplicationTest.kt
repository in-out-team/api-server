package com.inout.apiserver.application.study

import com.inout.apiserver.base.enums.FsrsCardState
import com.inout.apiserver.base.enums.LanguageType
import com.inout.apiserver.base.enums.LexicalCategoryType
import com.inout.apiserver.domain.study.Study
import com.inout.apiserver.domain.study.StudyService
import com.inout.apiserver.domain.word.Word
import com.inout.apiserver.domain.word.WordDefinition
import com.inout.apiserver.domain.word.WordService
import com.inout.apiserver.error.ConflictException
import com.inout.apiserver.error.NotFoundException
import com.inout.apiserver.interfaces.web.v1.request.CreateStudyRequest
import com.inout.fsrs.model.Card
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.time.Instant

class CreateStudyApplicationTest {
    private val studyService = mockk<StudyService>()
    private val wordService = mockk<WordService>()
    private val createStudyApplication = CreateStudyApplication(studyService, wordService)
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

    private fun createStudy(
        word: Word,
        userId: Long,
    ): Study {
        val fsrsCard = Card.createEmptyCard()
        return Study(
            id = 1L,
            userId = userId,
            wordDefinitionId = word.definitions.first().id,
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
            createdAt = now,
            updatedAt = now,
        )
    }

    @Test
    fun `run - should raise error when word definition does not exist`() {
        // Given
        val request =
            CreateStudyRequest(
                wordDefinitionId = 1L,
            )
        every { wordService.getWordByWordDefinitionId(request.wordDefinitionId) } throws
            NotFoundException(
                message = "Word Definition not found",
                code = "WORD_2",
            )

        // When
        val sut =
            assertThrows(NotFoundException::class.java) {
                createStudyApplication.run(request, 1L)
            }

        // Then
        assertEquals("Word Definition not found", sut.message)
        assertEquals("WORD_2", sut.code)
    }

    @Test
    fun `run - should raise error when study creation fails`() {
        // Given
        val request =
            CreateStudyRequest(
                wordDefinitionId = 1L,
            )
        val word = createWords(1).first()
        every { wordService.getWordByWordDefinitionId(request.wordDefinitionId) } returns word
        every { studyService.createStudy(any()) } throws
            ConflictException(
                message = "Study already exists",
                code = "STUDY_1",
            )

        // When
        val sut =
            assertThrows(ConflictException::class.java) {
                createStudyApplication.run(request, 1L)
            }

        // Then
        assertEquals("Study already exists", sut.message)
        assertEquals("STUDY_1", sut.code)
    }

    @Test
    fun `run - should return created study word`() {
        // Given
        val request =
            CreateStudyRequest(
                wordDefinitionId = 1L,
            )
        val word = createWords(1).first()
        val userId = 1L
        val study = createStudy(word, userId)
        every { wordService.getWordByWordDefinitionId(request.wordDefinitionId) } returns word
        every { studyService.createStudy(any()) } returns study

        // When
        val sut = createStudyApplication.run(request, userId)

        // Then
        assertEquals(study, sut.study)
        assertEquals(word, sut.word)
    }
}

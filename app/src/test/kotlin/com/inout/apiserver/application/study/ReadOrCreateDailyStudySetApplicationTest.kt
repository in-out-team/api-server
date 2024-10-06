package com.inout.apiserver.application.study

import com.inout.apiserver.domain.study.DailyStudySet
import com.inout.apiserver.domain.study.StudyFactory
import com.inout.apiserver.domain.study.StudyService
import com.inout.apiserver.domain.word.WordFactory
import com.inout.apiserver.domain.word.WordService
import com.inout.apiserver.error.BadRequestException
import com.inout.apiserver.error.NotFoundException
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.time.LocalDate

class ReadOrCreateDailyStudySetApplicationTest {
    private val studyService = mockk<StudyService>()
    private val wordService = mockk<WordService>()
    private val readOrCreateDailyStudySetApplication = ReadOrCreateDailyStudySetApplication(studyService, wordService)

    private fun mockStudyServiceGetDailyStudySetNull() {
        every { studyService.getDailyStudySet(any(), any()) } returns null
    }

    private fun mockCreateEmptyDailyStudySet() {
        every { studyService.getStudiesByIds(any()) } returns emptyList()
        every { studyService.getStudiesPastDue(any(), any(), any()) } returns listOf(StudyFactory.createStudy())
        every { wordService.getWordsByWordDefinitionIds(any()) } returns listOf(WordFactory.createWord())
        every { studyService.createDailyStudySet(any()) } returns
            DailyStudySet(
                id = 1L,
                userId = 1L,
                date = LocalDate.now(),
                studyIds = emptyList(),
            )
    }

    private fun mockStudyServiceGetDailyStudySet() {
        every { studyService.getDailyStudySet(any(), any()) } returns
            DailyStudySet(
                id = 1L,
                userId = 1L,
                date = LocalDate.now(),
                studyIds = listOf(1L),
            )
        every { studyService.getStudiesByIds(any()) } returns listOf(StudyFactory.createStudy())
        every { wordService.getWordsByWordDefinitionIds(any()) } returns listOf(WordFactory.createWord())
    }

    @Test
    fun `if provided date is after today, then throw BadRequestException`() {
        // given
        val userId = 1L
        val date = LocalDate.now().plusDays(1)

        // when
        val exception =
            assertThrows(BadRequestException::class.java) {
                readOrCreateDailyStudySetApplication.run(userId, date)
            }

        // then
        assertEquals("Cannot request future daily study set", exception.message)
        assertEquals("STUDY_3", exception.code)
    }

    @Test
    fun `if provided date is not today but dailyStudySet is null, then throw NotFoundException`() {
        // given
        val userId = 1L
        val date = LocalDate.now().minusDays(1)
        mockStudyServiceGetDailyStudySetNull()

        // when
        val exception =
            assertThrows(NotFoundException::class.java) {
                readOrCreateDailyStudySetApplication.run(userId, date)
            }

        // then
        assertEquals("DailyStudySet not found", exception.message)
        assertEquals("STUDY_4", exception.code)
        verify(exactly = 1) { studyService.getDailyStudySet(userId, date) }
    }

    @Test
    fun `should create dailyStudySet if it does not exist`() {
        // given
        val userId = 1L
        val date = LocalDate.now()
        mockStudyServiceGetDailyStudySetNull()
        mockCreateEmptyDailyStudySet()

        // when
        readOrCreateDailyStudySetApplication.run(userId, date)

        // then
        verify(exactly = 1) { studyService.createDailyStudySet(any()) }
    }

    @Test
    fun `returned dailyStudySet should have studyIds of target studies`() {
        // given
        val userId = 1L
        val date = LocalDate.now()
        mockStudyServiceGetDailyStudySetNull()
        mockCreateEmptyDailyStudySet()

        // when
        val result = readOrCreateDailyStudySetApplication.run(userId, date)

        // then
        assertEquals(1, result.dailyStudySet.studyIds.size)
        assertEquals(1L, result.dailyStudySet.studyIds[0])
    }

    @Test
    fun `returned studyWords should return target study words`() {
        // given
        val userId = 1L
        val date = LocalDate.now()
        mockStudyServiceGetDailyStudySetNull()
        mockCreateEmptyDailyStudySet()

        // when
        val result = readOrCreateDailyStudySetApplication.run(userId, date)

        // then
        assertEquals(1, result.studyWords.size)
        assertEquals(1L, result.studyWords[0].study.id)
    }

    @Test
    fun `should return past dailyStudySet if it exists`() {
        // given
        val userId = 1L
        val date = LocalDate.now().minusDays(1)
        mockStudyServiceGetDailyStudySet()

        // when
        val result = readOrCreateDailyStudySetApplication.run(userId, date)

        // then
        assertEquals(1, result.dailyStudySet.studyIds.size)
        assertEquals(1L, result.dailyStudySet.studyIds[0])
        assertEquals(1, result.studyWords.size)
        assertEquals(1L, result.studyWords[0].study.id)
    }
}

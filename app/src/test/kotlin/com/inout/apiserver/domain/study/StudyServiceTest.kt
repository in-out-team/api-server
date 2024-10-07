package com.inout.apiserver.domain.study

import com.inout.apiserver.base.enums.FsrsCardState
import com.inout.apiserver.error.ConflictException
import com.inout.apiserver.infrastructure.db.study.DailyStudySetRepository
import com.inout.apiserver.infrastructure.db.study.StudyRepository
import com.inout.fsrs.model.Card
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class StudyServiceTest {
    private val studyRepository = mockk<StudyRepository>()
    private val dailyStudySetRepository = mockk<DailyStudySetRepository>()
    private val studyService = StudyService(studyRepository, dailyStudySetRepository)
    private val now = Instant.now()

    private fun createStudies(count: Int): List<Study> {
        val fsrsCard = Card.createEmptyCard()
        return (1..count).map {
            Study(
                id = it.toLong(),
                userId = 1L,
                wordDefinitionId = it.toLong(),
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
    }

    private fun createDailyStudySet(
        userId: Long,
        date: Instant,
        studyIds: List<Long>,
    ): DailyStudySet {
        val localDate = LocalDate.ofInstant(date, date.atZone(ZoneId.systemDefault()).offset)
        return DailyStudySet(id = 1L, userId = userId, studyIds = studyIds, date = localDate)
    }

    @Nested
    inner class GetAllByUserId {
        @Test
        fun `should return empty list when there is no study`() {
            // given
            every { studyRepository.findAllByUserId(1L, any()) } returns Page.empty()

            // when
            val sut = studyService.getAllByUserId(1L, PageRequest.of(0, 10))

            // then
            assertTrue(sut.isEmpty)
        }

        @Test
        fun `should return list of studies`() {
            // given
            val studies = createStudies(3)
            every { studyRepository.findAllByUserId(1L, any()) } returns PageImpl(studies)

            // when
            val sut = studyService.getAllByUserId(1L, PageRequest.of(0, 10))

            // then
            assertEquals(3, sut.size)
            assertEquals(studies, sut.content)
        }
    }

    @Nested
    inner class GetByUserIdAndWordDefinitionId {
        @Test
        fun `should return study when it exists`() {
            // given
            val study = createStudies(1).first()
            every { studyRepository.findByUserIdAndWordDefinitionId(1L, 1L) } returns study

            // when
            val sut = studyService.getByUserIdAndWordDefinitionId(1L, 1L)

            // then
            assertEquals(study, sut)
        }

        @Test
        fun `should return null when study does not exist`() {
            // given
            every { studyRepository.findByUserIdAndWordDefinitionId(1L, 1L) } returns null

            // when
            val sut = studyService.getByUserIdAndWordDefinitionId(1L, 1L)

            // then
            assertNull(sut)
        }
    }

    @Nested
    inner class GetById {
        @Test
        fun `should return study when it exists`() {
            // given
            val study = createStudies(1).first()
            every { studyRepository.findById(1L) } returns study

            // when
            val sut = studyService.getById(1L)

            // then
            assertEquals(study, sut)
        }

        @Test
        fun `should return null when study does not exist`() {
            // given
            every { studyRepository.findById(1L) } returns null

            // when
            val sut = studyService.getById(1L)

            // then
            assertNull(sut)
        }
    }

    @Nested
    inner class CreateStudy {
        @Test
        fun `should throw ConflictException when study already exists`() {
            // given
            val studyCreateObject =
                StudyCreateObject(
                    userId = 1L,
                    wordDefinitionId = 1L,
                )
            every { studyRepository.findByUserIdAndWordDefinitionId(1L, 1L) } returns
                createStudies(1).first()

            // when
            val exception =
                assertThrows(ConflictException::class.java) {
                    studyService.createStudy(studyCreateObject)
                }

            // then
            assertEquals("Study already exists", exception.message)
            assertEquals("STUDY_1", exception.code)
        }

        @Test
        fun `should create study when it does not exist`() {
            // given
            val studyCreateObject =
                StudyCreateObject(
                    userId = 1L,
                    wordDefinitionId = 1L,
                )
            every { studyRepository.findByUserIdAndWordDefinitionId(1L, 1L) } returns null
            every { studyRepository.save(any()) } returns createStudies(1).first()

            // when
            val sut = studyService.createStudy(studyCreateObject)

            // then
            assertEquals(1L, sut.id)
            assertEquals(1L, sut.userId)
            assertEquals(1L, sut.wordDefinitionId)
            assertEquals(now, sut.createdAt)
            assertEquals(now, sut.updatedAt)
        }
    }

    @Nested
    inner class GetStudiesPastDue {
        @Test
        fun `should return empty list when count is 0 or less`() {
            listOf(0, -1).forEach { count ->
                // when
                val sut = studyService.getStudiesPastDue(1L, now, emptyList(), count)

                // then
                assertTrue(sut.isEmpty())
            }
        }

        @Test
        fun `should return list of studies`() {
            // given
            val studies = createStudies(3)
            every { studyRepository.findAllPastDueStudiesBy(1L, now, emptyList(), any()) } returns PageImpl(studies)

            // when
            val sut = studyService.getStudiesPastDue(1L, now, emptyList(), 3)

            // then
            assertEquals(3, sut.size)
            assertEquals(studies, sut)
        }
    }

    @Nested
    inner class GetStudiesByIds {
        @Test
        fun `should return empty list when ids is empty`() {
            // given
            every { studyRepository.findAllByIds(emptyList()) } returns emptyList()

            // when
            val sut = studyService.getStudiesByIds(emptyList())

            // then
            assertTrue(sut.isEmpty())
        }

        @Test
        fun `should return list of studies`() {
            // given
            val studies = createStudies(3)
            val ids = studies.map { it.id!! }
            every { studyRepository.findAllByIds(ids) } returns studies

            // when
            val sut = studyService.getStudiesByIds(ids)

            // then
            assertEquals(3, sut.size)
            assertEquals(studies, sut)
        }
    }

    @Nested
    inner class GetDailyStudySet {
        @Test
        fun `should return null when daily study set does not exist`() {
            // given
            every { dailyStudySetRepository.findByUserIdAndDate(1L, any()) } returns null

            // when
            val sut = studyService.getDailyStudySet(1L, LocalDate.now())

            // then
            assertNull(sut)
        }

        @Test
        fun `should return daily study set when it exists`() {
            // given
            val dailyStudySet = createDailyStudySet(1L, now, listOf(1L, 2L))
            every { dailyStudySetRepository.findByUserIdAndDate(1L, any()) } returns dailyStudySet

            // when
            val sut = studyService.getDailyStudySet(1L, LocalDate.now())

            // then
            assertEquals(dailyStudySet, sut)
        }
    }

    @Nested
    inner class CreateDailyStudySet {
        @Test
        fun `should throw ConflictException when daily study set already exists`() {
            // given
            val dailyStudySetCreateObject =
                DailyStudySetCreateObject(
                    userId = 1L,
                    date = LocalDate.now(),
                )
            every { dailyStudySetRepository.findByUserIdAndDate(1L, any()) } returns
                createDailyStudySet(1L, now, listOf(1L, 2L))

            // when
            val exception =
                assertThrows(ConflictException::class.java) {
                    studyService.createDailyStudySet(dailyStudySetCreateObject)
                }

            // then
            assertEquals("Daily study set already exists", exception.message)
            assertEquals("STUDY_5", exception.code)
        }

        @Test
        fun `should create daily study set when it does not exist`() {
            // given
            val dailyStudySetCreateObject =
                DailyStudySetCreateObject(
                    userId = 1L,
                    date = LocalDate.now(),
                )
            every { dailyStudySetRepository.findByUserIdAndDate(1L, any()) } returns null
            val dailyStudySet = createDailyStudySet(1L, now, listOf(1L, 2L))
            every { dailyStudySetRepository.save(any()) } returns dailyStudySet

            // when
            val sut = studyService.createDailyStudySet(dailyStudySetCreateObject)

            // then
            assertEquals(dailyStudySet, sut)
        }
    }

    @Nested
    inner class GetStudiesByDailyStudySet {
        @Test
        fun `should return daily study set studies when date is before today`() {
            // given
            val studies = createStudies(2)
            val dailyStudySet =
                createDailyStudySet(1L, now, studies.map { it.id }).copy(date = LocalDate.now().minusDays(1))
            every { studyRepository.findAllByIds(dailyStudySet.studyIds) } returns studies

            // when
            val sut = studyService.getStudiesByDailyStudySet(dailyStudySet)

            // then
            assertEquals(2, sut.size)
            assertEquals(studies, sut)
        }

        @Test
        fun `should return daily study set studies and past due studies when date is today`() {
            // given
            val studies = createStudies(2)
            val dailyStudySet = createDailyStudySet(1L, now, studies.map { it.id })
            every { studyRepository.findAllByIds(dailyStudySet.studyIds) } returns studies
            val pastDueStudies = createStudies(1)
            every { studyRepository.findAllPastDueStudiesBy(any(), any(), any(), any()) } returns PageImpl(pastDueStudies)

            // when
            val sut = studyService.getStudiesByDailyStudySet(dailyStudySet)

            // then
            assertEquals(3, sut.size)
            assertEquals(studies + pastDueStudies, sut)
        }
    }
}

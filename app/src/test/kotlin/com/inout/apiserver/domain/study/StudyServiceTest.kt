package com.inout.apiserver.domain.study

import com.inout.apiserver.error.ConflictException
import com.inout.apiserver.infrastructure.db.study.StudyRepository
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.time.LocalDateTime

class StudyServiceTest {
    private val studyRepository = mockk<StudyRepository>()
    private val studyService = StudyService(studyRepository)
    private val now = LocalDateTime.now()

    private fun createStudies(count: Int): List<Study> {
        return (1..count).map {
            Study(
                id = it.toLong(),
                userId = 1L,
                wordDefinitionId = it.toLong(),
                createdAt = now,
                updatedAt = now
            )
        }
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
            val studyCreateObject = StudyCreateObject(
                userId = 1L,
                wordDefinitionId = 1L
            )
            every { studyRepository.findByUserIdAndWordDefinitionId(1L, 1L) } returns Study(
                id = 1L,
                userId = 1L,
                wordDefinitionId = 1L,
                createdAt = now,
                updatedAt = now
            )

            // when
            val exception = assertThrows(ConflictException::class.java) {
                studyService.createStudy(studyCreateObject)
            }

            // then
            assertEquals("Study already exists", exception.message)
            assertEquals("STUDY_1", exception.code)
        }

        @Test
        fun `should create study when it does not exist`() {
            // given
            val studyCreateObject = StudyCreateObject(
                userId = 1L,
                wordDefinitionId = 1L
            )
            every { studyRepository.findByUserIdAndWordDefinitionId(1L, 1L) } returns null
            every { studyRepository.save(any()) } returns Study(
                id = 1L,
                userId = 1L,
                wordDefinitionId = 1L,
                createdAt = now,
                updatedAt = now
            )

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
}
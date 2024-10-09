package com.inout.apiserver.infrastructure.db.study

import com.inout.apiserver.domain.study.StudyCreateObject
import com.inout.apiserver.infrastructure.db.DbTestSupport
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageRequest
import java.time.Instant

@Import(StudyRepository::class)
class StudyRepositoryTest(
    private val studyRepository: StudyRepository,
    private val studyJpaRepository: StudyJpaRepository,
) : DbTestSupport() {
    private fun createUnsavedStudyEntity(
        userId: Long,
        wordDefinitionId: Long,
    ): StudyEntity {
        return StudyEntity.fromCreateObject(
            StudyCreateObject(
                userId = userId,
                wordDefinitionId = wordDefinitionId,
            ),
        )
    }

    @Nested
    inner class Save {
        @Test
        fun `should raise error when same user and word definition id exists`() {
            // given
            val userId = 1L
            val wordDefinitionId = 1L
            val studyEntity = createUnsavedStudyEntity(userId, wordDefinitionId)
            studyJpaRepository.save(studyEntity)

            // when
            val sut = createUnsavedStudyEntity(userId, wordDefinitionId)

            // then
            assertThatThrownBy { studyRepository.save(sut) }
                .isInstanceOf(Exception::class.java)
                .hasMessageContaining("could not execute statement")
        }

        @Test
        fun `should save study entity`() {
            // given
            val userId = 1L
            val wordDefinitionId = 1L
            val studyEntity = createUnsavedStudyEntity(userId, wordDefinitionId)

            // when
            val sut = studyRepository.save(studyEntity)

            // then
            assertNotNull(sut.id)
            assertEquals(userId, sut.userId)
            assertEquals(wordDefinitionId, sut.wordDefinitionId)
            assertNotNull(sut.createdAt)
            assertNotNull(sut.updatedAt)
        }
    }

    @Nested
    inner class FindByUserIdAndWordDefinitionId {
        @Test
        fun `should return null when study entity not exists`() {
            // given
            val userId = 1L
            val wordDefinitionId = 1L

            // when & then
            assertNull(studyRepository.findByUserIdAndWordDefinitionId(userId, wordDefinitionId))
        }

        @Test
        fun `should return study entity`() {
            // given
            val userId = 1L
            val wordDefinitionId = 1L
            val studyEntity = createUnsavedStudyEntity(userId, wordDefinitionId)
            studyJpaRepository.save(studyEntity)

            // when
            val sut = studyRepository.findByUserIdAndWordDefinitionId(userId, wordDefinitionId)

            // then
            assertNotNull(sut)
        }
    }

    @Nested
    inner class FindById {
        @Test
        fun `should return null when study entity not exists`() {
            // given
            val id = 1L

            // when & then
            assertNull(studyRepository.findById(id))
        }

        @Test
        fun `should return study entity`() {
            // given
            val userId = 1L
            val wordDefinitionId = 1L
            val studyEntity = createUnsavedStudyEntity(userId, wordDefinitionId)
            val savedStudy = studyJpaRepository.save(studyEntity)

            // when
            val sut = studyRepository.findById(savedStudy.id!!)

            // then
            assertNotNull(sut)
        }
    }

    @Nested
    inner class FindAllByUserId {
        @Test
        fun `should return empty page when study entity not exists`() {
            // given
            val userId = 1L
            val pageable = PageRequest.of(0, 10)

            // when
            val studies = studyRepository.findAllByUserId(userId, pageable)

            // then
            assertTrue(studies.isEmpty)
        }

        @Test
        fun `should return page of study entity`() {
            // given
            val userId = 1L
            val studies = (1..10).map { createUnsavedStudyEntity(userId, it.toLong()) }
            studyJpaRepository.saveAll(studies)

            // when
            val pageable1 = PageRequest.of(0, 6)
            val sut1 = studyRepository.findAllByUserId(userId, pageable1)
            val pageable2 = PageRequest.of(1, 6)
            val sut2 = studyRepository.findAllByUserId(userId, pageable2)

            // then
            assertEquals(10, sut1.totalElements)
            assertEquals(6, sut1.numberOfElements)
            assertEquals(6, sut1.content.size)
            assertEquals(true, sut1.hasNext())

            assertEquals(10, sut2.totalElements)
            assertEquals(4, sut2.numberOfElements)
            assertEquals(4, sut2.content.size)
            assertEquals(false, sut2.hasNext())
        }
    }

    @Nested
    inner class FindAllByIds {
        @Test
        fun `should return empty list when study entity not exists`() {
            // given
            val ids = emptyList<Long>()

            // when
            val sut = studyRepository.findAllByIds(ids)

            // then
            assertTrue(sut.isEmpty())
        }

        @Test
        fun `should return list of study entity`() {
            // given
            val userId = 1L
            val studies = (1..10).map { createUnsavedStudyEntity(userId, it.toLong()) }
            val savedStudies = studyJpaRepository.saveAll(studies)

            // when
            val ids = savedStudies.shuffled().take(5).map { it.id!! }
            val sut = studyRepository.findAllByIds(ids)

            // then
            assertEquals(5, sut.size)
            sut.forEach { study ->
                assertTrue(ids.contains(study.id))
            }
        }
    }

    @Nested
    inner class FindAllPastDueStudiesBy {
        @Test
        fun `should return empty page when study entity not exists`() {
            // given
            val userId = 1L
            val savedStudies = (1..10).map { createUnsavedStudyEntity(userId, it.toLong()) }
            studyJpaRepository.saveAll(savedStudies)
            val due = Instant.now()
            val studies = (11..20).map { createUnsavedStudyEntity(userId, it.toLong()) }
            studyJpaRepository.saveAll(studies)

            // when
            val pageable = PageRequest.of(0, 10)
            val sut = studyRepository.findAllPastDueStudiesBy(userId, due, emptyList(), pageable)

            // then
            assertEquals(10, sut.totalElements)
            assertEquals(10, sut.numberOfElements)
            assertEquals(10, sut.content.size)
            assertEquals(false, sut.hasNext())
        }

        @Test
        fun `should return page of study entity`() {
            // given
            val userId = 1L
            val studies = (1..10).map { createUnsavedStudyEntity(userId, it.toLong()) }
            studyJpaRepository.saveAll(studies)

            // when
            val due = Instant.now()
            val pageable1 = PageRequest.of(0, 6)
            val sut1 = studyRepository.findAllPastDueStudiesBy(userId, due, emptyList(), pageable1)
            val pageable2 = PageRequest.of(1, 6)
            val sut2 = studyRepository.findAllPastDueStudiesBy(userId, due, emptyList(), pageable2)

            // then
            assertEquals(10, sut1.totalElements)
            assertEquals(6, sut1.numberOfElements)
            assertEquals(6, sut1.content.size)
            assertEquals(true, sut1.hasNext())

            assertEquals(10, sut2.totalElements)
            assertEquals(4, sut2.numberOfElements)
            assertEquals(4, sut2.content.size)
            assertEquals(false, sut2.hasNext())
        }

        @Test
        fun `should exclude studies of excludeIds`() {
            // given
            val userId = 1L
            val studies = (1..10).map { createUnsavedStudyEntity(userId, it.toLong()) }
            val savedStudies = studyJpaRepository.saveAll(studies)
            val due = Instant.now()
            val excludeIds = savedStudies.shuffled().take(5).map { it.id!! }

            // when
            val pageable = PageRequest.of(0, 10)
            val sut = studyRepository.findAllPastDueStudiesBy(userId, due, excludeIds, pageable)

            // then
            assertEquals(5, sut.totalElements)
            assertEquals(5, sut.numberOfElements)
            assertEquals(5, sut.content.size)
            sut.content.forEach { study ->
                assertTrue(excludeIds.contains(study.id).not())
            }
        }
    }
}

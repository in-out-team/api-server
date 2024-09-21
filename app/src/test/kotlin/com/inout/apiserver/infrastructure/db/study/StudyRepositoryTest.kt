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
}

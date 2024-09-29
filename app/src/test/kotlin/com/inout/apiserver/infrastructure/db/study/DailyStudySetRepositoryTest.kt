package com.inout.apiserver.infrastructure.db.study

import com.inout.apiserver.domain.study.DailyStudySetCreateObject
import com.inout.apiserver.infrastructure.db.DbTestSupport
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.context.annotation.Import
import java.time.LocalDate

@Import(DailyStudySetRepository::class)
class DailyStudySetRepositoryTest(
    private val dailyStudySetRepository: DailyStudySetRepository,
    private val dailyStudySetJpaRepository: DailyStudySetJpaRepository,
) : DbTestSupport() {
    private fun createUnsavedDailyStudySetEntity(
        userId: Long,
        date: LocalDate,
    ): DailyStudySetEntity {
        return DailyStudySetEntity.fromCreateObject(
            DailyStudySetCreateObject(
                userId = userId,
                date = date,
            ),
        )
    }

    @Nested
    inner class Save {
        @Test
        fun `should save daily study set entity`() {
            // given
            val userId = 1L
            val date = LocalDate.now()
            val dailyStudySetEntity = createUnsavedDailyStudySetEntity(userId, date)

            // when
            val sut = dailyStudySetRepository.save(dailyStudySetEntity)

            // then
            assertNotNull(sut.id)
            assertEquals(userId, sut.userId)
            assertEquals(date, sut.date)
            assertEquals(0, sut.studyIds.size)
        }

        @Test
        fun `should raise error when same user and date exists`() {
            // given
            val userId = 1L
            val date = LocalDate.now()
            val dailyStudySetEntity = createUnsavedDailyStudySetEntity(userId, date)
            dailyStudySetJpaRepository.save(dailyStudySetEntity)

            // when
            val sut = createUnsavedDailyStudySetEntity(userId, date)

            // then
            assertThatThrownBy { dailyStudySetRepository.save(sut) }
                .isInstanceOf(Exception::class.java)
                .hasMessageContaining("could not execute statement")
        }
    }

    @Nested
    inner class FindByUserIdAndDate {
        @Test
        fun `should find daily study set entity by user id and date`() {
            // given
            val userId = 1L
            val date = LocalDate.now()
            val dailyStudySetEntity = createUnsavedDailyStudySetEntity(userId, date)
            dailyStudySetJpaRepository.save(dailyStudySetEntity)

            // when
            val sut = dailyStudySetRepository.findByUserIdAndDate(userId, date)!!

            // then
            assertEquals(userId, sut.userId)
            assertEquals(date, sut.date)
            assertEquals(0, sut.studyIds.size)
        }

        @Test
        fun `should return null when daily study set entity not found by user id and date`() {
            // given
            val userId = 1L
            val date = LocalDate.now()

            // when
            val sut = dailyStudySetRepository.findByUserIdAndDate(userId, date)

            // then
            assertNull(sut)
        }
    }
}

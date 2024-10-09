package com.inout.apiserver.application.study

import com.inout.apiserver.base.enums.FsrsCardRating
import com.inout.apiserver.base.enums.FsrsCardState
import com.inout.apiserver.domain.study.DailyStudySetCreateObject
import com.inout.apiserver.domain.study.StudyFactory
import com.inout.apiserver.domain.word.Word
import com.inout.apiserver.domain.word.WordFactory
import com.inout.apiserver.error.NotFoundException
import com.inout.apiserver.helper.BaseIntegrationTest
import com.inout.apiserver.infrastructure.db.study.DailyStudySetEntity
import com.inout.apiserver.infrastructure.db.study.DailyStudySetRepository
import com.inout.apiserver.infrastructure.db.study.StudyEntity
import com.inout.apiserver.infrastructure.db.study.StudyRepository
import com.inout.apiserver.infrastructure.db.word.WordEntity
import com.inout.apiserver.infrastructure.db.word.WordRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Instant
import java.time.LocalDate

class RateStudyWordApplicationTest(
    // usecase
    private val rateStudyWordApplication: RateStudyWordApplication,
    // repositories
    private val dailyStudySetRepository: DailyStudySetRepository,
    private val wordRepository: WordRepository,
    private val studyRepository: StudyRepository,
) : BaseIntegrationTest() {
    private fun createDailyStudySet(userId: Long) =
        dailyStudySetRepository.save(
            DailyStudySetEntity.fromCreateObject(
                DailyStudySetCreateObject(
                    userId = userId,
                    date = LocalDate.now(),
                ),
            ),
        )

    private fun createWord() =
        wordRepository.save(
            WordEntity.fromDomain(
                WordFactory.createWord(),
            ),
        )

    private fun createStudy(
        word: Word,
        userId: Long,
    ) = studyRepository.save(
        StudyEntity.fromDomain(
            StudyFactory.createStudy(
                userId = userId,
                wordDefinitionId = word.definitions.first().id,
            ),
        ),
    )

    @Test
    fun `should raise NotFoundException when dailyStudySet does not exist`() {
        // given
        val userId = 1L
        val dailyStudySetId = 1L
        val studyId = 1L
        val rating = FsrsCardRating.EASY
        assertNull(dailyStudySetRepository.findById(dailyStudySetId))

        // when
        val exception =
            assertThrows<NotFoundException> {
                rateStudyWordApplication.run(userId, dailyStudySetId, studyId, rating)
            }

        // then
        assertEquals("Daily study set not found", exception.message)
        assertEquals("STUDY_1", exception.code)
    }

    @Test
    fun `should raise NotFoundException when userId does not match dailyStudySet userId`() {
        // given
        val userId = 1L
        val dailyStudySetId = 1L
        val studyId = 1L
        val rating = FsrsCardRating.EASY
        createDailyStudySet(userId + 1L)

        // when
        val exception =
            assertThrows<NotFoundException> {
                rateStudyWordApplication.run(userId, dailyStudySetId, studyId, rating)
            }

        // then
        assertEquals("Daily study set not found", exception.message)
        assertEquals("STUDY_1", exception.code)
    }

    @Test
    fun `should raise NotFoundException when studyId does not exist in dailyStudySet`() {
        // given
        val userId = 1L
        val dailyStudySetId = 1L
        val studyId = 1L
        val rating = FsrsCardRating.EASY
        createDailyStudySet(userId)

        // when
        val exception =
            assertThrows<NotFoundException> {
                rateStudyWordApplication.run(userId, dailyStudySetId, studyId, rating)
            }

        // then
        assertEquals("studyId of 1 not found in dailyStudySetId of 1", exception.message)
        assertEquals("STUDY_2", exception.code)
    }

    @Test
    fun `should rate study`() {
        // given
        val userId = 1L
        val dailyStudySetId = 1L
        val rating = FsrsCardRating.EASY
        createDailyStudySet(userId)
        val word = createWord()
        val study = createStudy(word, userId)

        // when
        val result = rateStudyWordApplication.run(userId, dailyStudySetId, study.id, rating).studyWord.study

        // then
        assertEquals(FsrsCardState.REVIEW, result.state)
        assertThat(result.due).isAfter(study.due)
        assertThat(result.stability).isGreaterThan(study.stability)
        assertThat(result.difficulty).isGreaterThan(study.difficulty)
        assertThat(result.scheduledDays).isGreaterThan(study.scheduledDays)
        assertEquals(study.reps + 1, result.reps)
        assertThat(result.lastReview).isAfter(Instant.now().minusSeconds(1))
        assertEquals(result.reviewLogs.size, study.reviewLogs.size + 1)
    }

    @Test
    fun `should add study to dailyStudySet`() {
        // given
        val userId = 1L
        val dailyStudySetId = 1L
        val rating = FsrsCardRating.EASY
        val dailyStudySet =
            dailyStudySetRepository.save(
                DailyStudySetEntity.fromCreateObject(
                    DailyStudySetCreateObject(
                        userId = userId,
                        date = LocalDate.now(),
                    ),
                ),
            )
        assertThat(dailyStudySet.studyIds).isEmpty()
        val word = createWord()
        val study = createStudy(word, userId)

        // when
        rateStudyWordApplication.run(userId, dailyStudySetId, study.id, rating)

        // then
        val updatedDailyStudySet = dailyStudySetRepository.findById(dailyStudySetId)!!
        assertThat(updatedDailyStudySet.studyIds).contains(study.id)
    }
}

package com.inout.apiserver.domain.study

import com.inout.apiserver.error.ConflictException
import com.inout.apiserver.helper.InOutSpringBootTest
import com.inout.apiserver.infrastructure.db.study.DailyStudySetEntity
import com.inout.apiserver.infrastructure.db.study.DailyStudySetRepository
import com.inout.apiserver.infrastructure.db.study.StudyEntity
import com.inout.apiserver.infrastructure.db.study.StudyRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate

@InOutSpringBootTest
class StudyServiceIntgTest(
    // services
    private val studyService: StudyService,
    // repositories
    private val dailyStudySetRepository: DailyStudySetRepository,
    private val studyRepository: StudyRepository,
) {
    @Nested
    inner class AddStudyToDailyStudySet {
        @Test
        fun `should raise error if study is already in daily study set`() {
            // given
            val userId = 1L
            val study = studyRepository.save(StudyEntity.fromDomain(StudyFactory.createStudy()))
            val dailyStudySet =
                dailyStudySetRepository.save(
                    DailyStudySetEntity(
                        userId = userId,
                        date = LocalDate.now(),
                        studyIds = listOf(study.id),
                    ),
                )

            // when
            val exception =
                assertThrows<ConflictException> {
                    studyService.addStudyToDailyStudySet(dailyStudySet, study)
                }

            // then
            assertEquals("Study already exists in daily study set", exception.message)
            assertEquals("STUDY_7", exception.code)
        }

        @Test
        fun `should add study to daily study set`() {
            // given
            val userId = 1L
            val dailyStudySet =
                dailyStudySetRepository.save(
                    DailyStudySetEntity(
                        userId = userId,
                        date = LocalDate.now(),
                        studyIds = emptyList(),
                    ),
                )
            val study = studyRepository.save(StudyEntity.fromDomain(StudyFactory.createStudy()))

            // when
            studyService.addStudyToDailyStudySet(dailyStudySet, study)

            // then
            val updatedDailyStudySet = checkNotNull(dailyStudySetRepository.findById(dailyStudySet.id))
            assertEquals(1, updatedDailyStudySet.studyIds.size)
            assertEquals(study.id, updatedDailyStudySet.studyIds.first())
        }
    }
}

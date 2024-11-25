package com.inout.apiserver.domain.study

import com.inout.apiserver.error.ConflictException
import com.inout.apiserver.extension.cleanUp
import com.inout.apiserver.helper.InOutSpringBootTest
import com.inout.apiserver.infrastructure.db.study.DailyStudySetEntity
import com.inout.apiserver.infrastructure.db.study.DailyStudySetRepository
import com.inout.apiserver.infrastructure.db.study.StudyEntity
import com.inout.apiserver.infrastructure.db.study.StudyRepository
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.assertThrows
import org.springframework.jdbc.core.JdbcTemplate
import java.time.LocalDate

@InOutSpringBootTest
class StudyServiceIntgTest(
    // services
    private val studyService: StudyService,
    // repositories
    private val dailyStudySetRepository: DailyStudySetRepository,
    private val studyRepository: StudyRepository,
    // etc
    private val jdbcTemplate: JdbcTemplate,
) : DescribeSpec({
        beforeEach {
            jdbcTemplate.cleanUp()
        }

        describe("addStudyToDailyStudySet") {
            it("should raise error if study is already in daily study set") {
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
                exception.message shouldBe "Study already exists in daily study set"
                exception.code shouldBe "STUDY_7"
            }

            it("should add study to daily study set") {
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
                updatedDailyStudySet.studyIds.size shouldBe 1
                updatedDailyStudySet.studyIds.first() shouldBe study.id
            }
        }
    })

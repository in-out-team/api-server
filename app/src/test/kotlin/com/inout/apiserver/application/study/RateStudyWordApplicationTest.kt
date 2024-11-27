package com.inout.apiserver.application.study

import com.inout.apiserver.base.enums.FsrsCardRating
import com.inout.apiserver.base.enums.FsrsCardState
import com.inout.apiserver.domain.study.DailyStudySetCreateObject
import com.inout.apiserver.domain.study.StudyFactory
import com.inout.apiserver.domain.word.Word
import com.inout.apiserver.domain.word.WordFactory
import com.inout.apiserver.error.NotFoundException
import com.inout.apiserver.extension.cleanUp
import com.inout.apiserver.helper.InOutSpringBootTest
import com.inout.apiserver.infrastructure.db.study.DailyStudySetEntity
import com.inout.apiserver.infrastructure.db.study.DailyStudySetRepository
import com.inout.apiserver.infrastructure.db.study.StudyEntity
import com.inout.apiserver.infrastructure.db.study.StudyRepository
import com.inout.apiserver.infrastructure.db.word.WordEntity
import com.inout.apiserver.infrastructure.db.word.WordRepository
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.assertThrows
import org.springframework.jdbc.core.JdbcTemplate
import java.time.Instant
import java.time.LocalDate

@InOutSpringBootTest
class RateStudyWordApplicationTest(
    // usecase
    private val rateStudyWordApplication: RateStudyWordApplication,
    // repositories
    private val dailyStudySetRepository: DailyStudySetRepository,
    private val wordRepository: WordRepository,
    private val studyRepository: StudyRepository,
    // etc
    private val jdbcTemplate: JdbcTemplate,
) : DescribeSpec({
        fun createDailyStudySet(userId: Long) =
            dailyStudySetRepository.save(
                DailyStudySetEntity.fromCreateObject(
                    DailyStudySetCreateObject(
                        userId = userId,
                        date = LocalDate.now(),
                    ),
                ),
            )

        fun createWord() =
            wordRepository.save(
                WordEntity.fromDomain(
                    WordFactory.createWord(),
                ),
            )

        fun createStudy(
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

        afterEach {
            jdbcTemplate.cleanUp()
        }

        describe("when dailyStudySet does not exist") {
            it("should raise NotFoundException") {
                // given
                val userId = 1L
                val dailyStudySetId = 1L
                val studyId = 1L
                val rating = FsrsCardRating.EASY
                dailyStudySetRepository.findById(dailyStudySetId) shouldBe null

                // when
                val exception =
                    assertThrows<NotFoundException> {
                        rateStudyWordApplication.run(userId, dailyStudySetId, studyId, rating)
                    }

                // then
                exception.message shouldBe "Daily study set not found"
                exception.code shouldBe "STUDY_4"
            }
        }

        describe("when userId does not match dailyStudySet userId") {
            it("should raise NotFoundException") {
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
                exception.message shouldBe "Daily study set not found"
                exception.code shouldBe "STUDY_4"
            }
        }

        describe("when studyId does not exist in dailyStudySet") {
            it("should raise NotFoundException") {
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
                exception.message shouldBe "studyId of $studyId not found in dailyStudySetId of $dailyStudySetId"
                exception.code shouldBe "STUDY_2"
            }
        }

        describe("rating study") {
            it("should rate study") {
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
                result.state shouldBe FsrsCardState.REVIEW
                result.due shouldBeGreaterThan study.due
                result.stability shouldBeGreaterThan study.stability
                result.difficulty shouldBeGreaterThan study.difficulty
                result.scheduledDays shouldBeGreaterThan study.scheduledDays
                result.reps shouldBe study.reps + 1
                result.lastReview!! shouldBeGreaterThan Instant.now().minusSeconds(1)
                result.reviewLogs.size shouldBe study.reviewLogs.size + 1
            }

            it("should add study to dailyStudySet") {
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
                dailyStudySet.studyIds shouldBe emptyList()
                val word = createWord()
                val study = createStudy(word, userId)

                // when
                rateStudyWordApplication.run(userId, dailyStudySetId, study.id, rating)

                // then
                val updatedDailyStudySet = dailyStudySetRepository.findById(dailyStudySetId)!!
                updatedDailyStudySet.studyIds shouldContain study.id
            }
        }
    })

package com.inout.apiserver.application.study

import com.inout.apiserver.base.enums.FsrsCardRating
import com.inout.apiserver.base.enums.FsrsCardState
import com.inout.apiserver.domain.study.StudyFactory
import com.inout.apiserver.domain.user.UserFactory
import com.inout.apiserver.domain.word.WordFactory
import com.inout.apiserver.error.NotFoundException
import com.inout.apiserver.extension.cleanUp
import com.inout.apiserver.helper.InOutSpringBootTest
import com.inout.apiserver.infrastructure.db.study.DailyStudySetRepository
import com.inout.apiserver.infrastructure.db.study.Study
import com.inout.apiserver.infrastructure.db.study.StudyRepository
import com.inout.apiserver.infrastructure.db.user.User
import com.inout.apiserver.infrastructure.db.word.Word
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.assertThrows
import org.springframework.jdbc.core.JdbcTemplate
import java.time.Instant
import java.util.Optional

@InOutSpringBootTest
class RateStudyWordApplicationTest(
    private val subject: RateStudyWordApplication,
    // repositories
    private val dailyStudySetRepository: DailyStudySetRepository,
    private val studyRepository: StudyRepository,
    // factories
    private val studyFactory: StudyFactory,
    private val wordFactory: WordFactory,
    private val userFactory: UserFactory,
    // etc
    private val jdbcTemplate: JdbcTemplate,
) : DescribeSpec({
        var user: User? = null
        var word: Word? = null
        var study: Study? = null

        beforeEach {
            user = userFactory.createUser()
            word = wordFactory.createWord()
            study = studyFactory.createStudy(userId = user!!.id!!, wordDefinitionId = word!!.definitions.first().id!!)
        }

        afterEach {
            jdbcTemplate.cleanUp()
        }

        describe("when dailyStudySet does not exist") {
            it("should raise NotFoundException") {
                // given
                val dailyStudySetId = 1L
                val rating = FsrsCardRating.EASY
                dailyStudySetRepository.findById(dailyStudySetId) shouldBe Optional.empty()

                // when
                val exception =
                    assertThrows<NotFoundException> {
                        subject.run(
                            RateStudyWordApplication.Request(
                                userId = user!!.id!!,
                                dailyStudySetId = dailyStudySetId,
                                studyId = study!!.id!!,
                                rating = rating,
                            ),
                        )
                    }

                // then
                exception.message shouldBe "Daily study set not found"
                exception.code shouldBe "STUDY_4"
            }
        }

        describe("when userId does not match dailyStudySet userId") {
            var otherUser: User? = null
            var otherUserStudy: Study? = null

            beforeEach {
                otherUser = userFactory.createUser(email = "test2@1.com")
                otherUserStudy =
                    studyFactory.createStudy(
                        userId = otherUser!!.id!!,
                        wordDefinitionId = word!!.definitions.first().id!!,
                    )
            }

            it("should raise NotFoundException") {
                // given
                val rating = FsrsCardRating.EASY
                val otherUserDailyStudySet =
                    studyFactory.createDailyStudySet(userId = otherUser!!.id!!, studies = listOf(study!!))

                // when
                val exception =
                    assertThrows<NotFoundException> {
                        subject.run(
                            RateStudyWordApplication.Request(
                                userId = user!!.id!!,
                                dailyStudySetId = otherUserDailyStudySet.id!!,
                                studyId = otherUserStudy!!.id!!,
                                rating = rating,
                            ),
                        )
                    }

                // then
                exception.message shouldBe "Daily study set not found"
                exception.code shouldBe "STUDY_4"
            }
        }

        describe("when studyId does not exist in dailyStudySet") {
            it("should raise NotFoundException") {
                // given
                val rating = FsrsCardRating.EASY
                val dailyStudySet = studyFactory.createDailyStudySet(userId = user!!.id!!, studies = listOf(study!!))
                val studyId = 2L
                studyRepository.findById(studyId) shouldBe Optional.empty()

                // when
                val exception =
                    assertThrows<NotFoundException> {
                        subject.run(
                            RateStudyWordApplication.Request(
                                userId = user!!.id!!,
                                dailyStudySetId = dailyStudySet.id!!,
                                studyId = studyId,
                                rating = rating,
                            ),
                        )
                    }

                // then
                exception.message shouldBe "studyId of $studyId not found in dailyStudySetId of ${dailyStudySet.id}"
                exception.code shouldBe "STUDY_2"
            }
        }

        describe("rating study") {
            it("should rate study") {
                // given
                val rating = FsrsCardRating.EASY
                val dailyStudySet = studyFactory.createDailyStudySet(userId = user!!.id!!)

                // when
                val result =
                    subject
                        .run(
                            RateStudyWordApplication.Request(
                                userId = user!!.id!!,
                                dailyStudySetId = dailyStudySet.id!!,
                                studyId = study!!.id!!,
                                rating = rating,
                            ),
                        ).studyWord.study

                // then
                result.state shouldBe FsrsCardState.REVIEW
                result.due shouldBeGreaterThan study!!.due
                result.stability shouldBeGreaterThan study!!.stability
                result.difficulty shouldBeGreaterThan study!!.difficulty
                result.scheduledDays shouldBeGreaterThan study!!.scheduledDays
                result.reps shouldBe study!!.reps + 1
                result.lastReview!! shouldBeGreaterThan Instant.now().minusSeconds(1)
                result.reviewLogs.size shouldBe study!!.reviewLogs.size + 1
            }

            it("should add study to dailyStudySet") {
                // given
                val rating = FsrsCardRating.EASY
                val dailyStudySet = studyFactory.createDailyStudySet(userId = user!!.id!!)
                dailyStudySet.studyIds shouldBe emptyList()

                // when
                subject.run(
                    RateStudyWordApplication.Request(
                        userId = user!!.id!!,
                        dailyStudySetId = dailyStudySet.id!!,
                        studyId = study!!.id!!,
                        rating = rating,
                    ),
                )

                // then
                val updatedDailyStudySet = dailyStudySetRepository.findById(dailyStudySet.id!!).orElseThrow()
                updatedDailyStudySet.studyIds shouldContain study!!.id!!
            }
        }
    })

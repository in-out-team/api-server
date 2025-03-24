package com.inout.apiserver.application.study

import com.inout.apiserver.base.enums.LexicalCategoryType
import com.inout.apiserver.domain.study.StudyFactory
import com.inout.apiserver.domain.study.StudyService
import com.inout.apiserver.domain.user.UserFactory
import com.inout.apiserver.domain.word.WordFactory
import com.inout.apiserver.error.BadRequestException
import com.inout.apiserver.error.NotFoundException
import com.inout.apiserver.extension.cleanUp
import com.inout.apiserver.helper.InOutSpringBootTest
import com.inout.apiserver.infrastructure.db.user.User
import com.inout.apiserver.infrastructure.db.word.Word
import com.inout.apiserver.infrastructure.db.word.WordDefinition
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.springframework.jdbc.core.JdbcTemplate
import java.time.LocalDate
import java.time.ZoneId

@InOutSpringBootTest
class ReadOrCreateDailyStudySetApplicationTest(
    private val subject: ReadOrCreateDailyStudySetApplication,
    // services
    private val studyService: StudyService,
    // factories
    private val userFactory: UserFactory,
    private val wordFactory: WordFactory,
    private val studyFactory: StudyFactory,
    // etc
    private val jdbcTemplate: JdbcTemplate,
) : DescribeSpec({
        var user: User? = null
        var word: Word? = null

        beforeEach {
            user = userFactory.createUser()
            word =
                wordFactory.createWord(
                    wordDefinitions =
                        listOf(
                            WordDefinition(
                                lexicalCategory = LexicalCategoryType.NOUN,
                                meaning = "책",
                                preContext = "정보를 얻거나 즐거움을 얻기 위해 읽는 인쇄물",
                            ),
                            WordDefinition(
                                lexicalCategory = LexicalCategoryType.VERB,
                                meaning = "예약하다",
                                preContext = "특정한 날짜나 시간에 무엇을 하기 위해 미리 자리를 확보하다",
                            ),
                        ),
                )
        }

        afterEach {
            jdbcTemplate.cleanUp()
        }

        describe("if provided date is after today") {
            it("should throw BadRequestException") {
                // given
                val date = LocalDate.now().plusDays(1)

                // when
                val exception =
                    shouldThrow<BadRequestException> {
                        subject.run(
                            ReadOrCreateDailyStudySetApplication.Request(
                                user = user!!,
                                date = date,
                            ),
                        )
                    }

                // then
                exception.message shouldBe "Cannot request future daily study set"
                exception.code shouldBe "STUDY_3"
            }
        }

        describe("if provided date is past today") {
            it("should throw NotFoundException") {
                // given
                val date = LocalDate.now().minusDays(1)

                // when
                val exception =
                    shouldThrow<NotFoundException> {
                        subject.run(
                            ReadOrCreateDailyStudySetApplication.Request(
                                user = user!!,
                                date = date,
                            ),
                        )
                    }

                // then
                exception.message shouldBe "DailyStudySet not found"
                exception.code shouldBe "STUDY_4"
            }
        }

        describe("if provided date is today") {
            it("should create dailyStudySet if it does not exist") {
                // given
                val date = LocalDate.now()
                studyService.getDailyStudySet(user!!.id!!, date) shouldBe null

                // when
                subject.run(
                    ReadOrCreateDailyStudySetApplication.Request(
                        user = user!!,
                        date = date,
                    ),
                )

                // then
                studyService.getDailyStudySet(user!!.id!!, date) shouldNotBe null
            }

            it("returned dailyStudySet should have studyIds of target studies") {
                // given
                val userZoneId = ZoneId.of(user!!.timezone)
                val date = LocalDate.now(userZoneId)
                val studies =
                    word!!.definitions.map {
                        studyFactory.createStudy(
                            userId = user!!.id!!,
                            wordDefinitionId = it.id!!,
                        )
                    }
                studyFactory.createDailyStudySet(user!!, date, studies)

                // when
                val result =
                    subject.run(
                        ReadOrCreateDailyStudySetApplication.Request(
                            user = user!!,
                            date = date,
                        ),
                    )

                // then
                result.dailyStudySet.studyIds shouldBe studies.map { it.id!! }
            }

            it("returned studyWords should return target study words") {
                // given
                val userZoneId = ZoneId.of(user!!.timezone)
                val date = LocalDate.now(userZoneId)
                val studies =
                    word!!.definitions.map {
                        studyFactory.createStudy(
                            userId = user!!.id!!,
                            wordDefinitionId = it.id!!,
                        )
                    }
                studyFactory.createDailyStudySet(user!!, date, studies)

                // when
                val result =
                    subject.run(
                        ReadOrCreateDailyStudySetApplication.Request(
                            user = user!!,
                            date = date,
                        ),
                    )

                // then
                result.studyWords.size shouldBe studies.size
                result.studyWords.map { it.study.id!! } shouldBe studies.map { it.id!! }
            }
        }

        describe("if provided date is before today") {
            it("should return past dailyStudySet if it exists") {
                // given
                val userZoneId = ZoneId.of(user!!.timezone)
                val date = LocalDate.now(userZoneId).minusDays(1)
                val studies =
                    word!!.definitions.map {
                        studyFactory.createStudy(
                            userId = user!!.id!!,
                            wordDefinitionId = it.id!!,
                        )
                    }
                studyFactory.createDailyStudySet(user!!, date, studies)

                // when
                val result =
                    subject.run(
                        ReadOrCreateDailyStudySetApplication.Request(
                            user = user!!,
                            date = date,
                        ),
                    )

                // then
                result.dailyStudySet.studyIds shouldBe studies.map { it.id!! }
                result.studyWords.size shouldBe studies.size
                result.studyWords.map { it.study.id!! } shouldBe studies.map { it.id!! }
            }
        }
    })

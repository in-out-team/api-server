package com.inout.apiserver.domain.study

import com.inout.apiserver.base.enums.FsrsCardRating
import com.inout.apiserver.base.enums.FsrsCardState
import com.inout.apiserver.base.enums.LexicalCategoryType
import com.inout.apiserver.domain.user.UserFactory
import com.inout.apiserver.domain.word.WordDefinitionCreateObject
import com.inout.apiserver.domain.word.WordFactory
import com.inout.apiserver.error.BadRequestException
import com.inout.apiserver.error.ConflictException
import com.inout.apiserver.extension.cleanUp
import com.inout.apiserver.helper.InOutSpringBootTest
import com.inout.apiserver.infrastructure.db.study.DailyStudySetRepository
import com.inout.apiserver.infrastructure.db.study.Study
import com.inout.apiserver.infrastructure.db.user.User
import com.inout.apiserver.infrastructure.db.word.Word
import com.inout.apiserver.infrastructure.db.word.WordDefinition
import com.inout.fsrs.base.plusDays
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeSortedBy
import io.kotest.matchers.collections.shouldBeSortedDescendingBy
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldContainAnyOf
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.ranges.shouldBeIn
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.assertThrows
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.jdbc.core.JdbcTemplate
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.util.Optional

@InOutSpringBootTest
class StudyServiceTest(
    // services
    private val studyService: StudyService,
    // repositories
    private val dailyStudySetRepository: DailyStudySetRepository,
    // factories
    private val studyFactory: StudyFactory,
    private val userFactory: UserFactory,
    private val wordFactory: WordFactory,
    // etc
    private val jdbcTemplate: JdbcTemplate,
) : DescribeSpec({
        var user: User? = null

        beforeEach {
            user = userFactory.createUser()
        }

        afterEach {
            jdbcTemplate.cleanUp()
        }

        describe("getAllByUserId") {
            fun createWords(): List<Word> =
                listOf(
                    wordFactory
                        .createWord(
                            name = "bank",
                            wordDefinitions =
                                listOf(
                                    WordDefinitionCreateObject(
                                        lexicalCategory = LexicalCategoryType.NOUN,
                                        meaning = "은행",
                                        preContext = "돈을 보관하거나 대출을 해주는 기관",
                                    ),
                                ),
                        ),
                    wordFactory
                        .createWord(
                            name = "book",
                            wordDefinitions =
                                listOf(
                                    WordDefinitionCreateObject(
                                        lexicalCategory = LexicalCategoryType.NOUN,
                                        meaning = "책",
                                        preContext = "정보를 얻거나 즐거움을 얻기 위해 읽는 인쇄물",
                                    ),
                                    WordDefinitionCreateObject(
                                        lexicalCategory = LexicalCategoryType.VERB,
                                        meaning = "예약하다",
                                        preContext = "특정한 날짜나 시간에 무엇을 하기 위해 미리 자리를 확보하다",
                                    ),
                                ),
                        ),
                    wordFactory
                        .createWord(
                            name = "booked",
                            wordDefinitions =
                                listOf(
                                    WordDefinitionCreateObject(
                                        lexicalCategory = LexicalCategoryType.NOUN,
                                        meaning = "예약된",
                                        preContext = "미리 자리를 확보한",
                                    ),
                                ),
                        ),
                )

            fun createStudies(
                user: User,
                wordDefinitions: List<WordDefinition>,
            ): List<Study> = wordDefinitions.map { studyFactory.createStudy(userId = user.id!!, wordDefinitionId = it.id!!) }

            it("should return empty list when there is no study") {
                // given
                val pageable = PageRequest.of(0, 10)

                // when
                val studies = studyService.getAllByUserId(user!!.id!!, null, pageable)

                // then
                studies.isEmpty shouldBe true
            }

            it("should return list of studies with given/default sort") {
                // given
                val words = createWords()
                createStudies(user!!, words.flatMap { it.definitions })

                // case1: sort by createdAt descending
                val res1 =
                    studyService.getAllByUserId(user!!.id!!, null, PageRequest.of(0, 10, Sort.by("createdAt").descending()))
                res1.totalElements shouldBe 4
                res1.content shouldBeSortedDescendingBy { it.createdAt!! }

                // case 2: sort by createdAt ascending
                val res2 =
                    studyService.getAllByUserId(user!!.id!!, null, PageRequest.of(0, 10, Sort.by("createdAt").ascending()))
                res2.totalElements shouldBe 4
                res2.content shouldBeSortedBy { it.createdAt!! }

                // case 3: sort by default (due descending)
                val res3 =
                    studyService.getAllByUserId(user!!.id!!, null, PageRequest.of(0, 10))
                res3.totalElements shouldBe 4
                res3.content shouldBeSortedDescendingBy { it.due }

                // case 4: sort by invalid sort
                val res4 =
                    studyService.getAllByUserId(user!!.id!!, null, PageRequest.of(0, 10, Sort.by("invalid").descending()))
                res4.totalElements shouldBe 4
                res4.content shouldBeSortedDescendingBy { it.due }
            }

            it("should return list of studies with matching prefix") {
                // given
                val words = createWords()
                val studyingWordDefinitions = words.map { it.definitions.first() }
                createStudies(user!!, studyingWordDefinitions)
                val prefix = "book"

                // when
                val res = studyService.getAllByUserId(user!!.id!!, prefix, PageRequest.of(0, 2))

                // then
                val expectedWords = words.filter { it.name.startsWith(prefix) }
                res.totalElements shouldBe expectedWords.size
                val expectedWordDefinitions =
                    expectedWords
                        .flatMap { word -> word.definitions }
                        .map { wordDefinition -> wordDefinition.id!! }
                        .filter { wordDefinitionId -> wordDefinitionId in studyingWordDefinitions.map { it.id!! } }
                res.content.map { it.wordDefinitionId } shouldContainAll expectedWordDefinitions
                res.content shouldBeSortedBy { expectedWords.find { it.definitions.first().wordId == it.id }!!.name }
            }
        }

        describe("getByUserIdAndWordDefinitionId") {
            var word: Word?
            var study: Study? = null

            beforeEach {
                word = wordFactory.createWord()
                study = studyFactory.createStudy(userId = user!!.id!!, wordDefinitionId = word!!.definitions.first().id!!)
            }

            it("should return study when it exists") {
                // when
                val res = studyService.getByUserIdAndWordDefinitionId(user!!.id!!, study!!.wordDefinitionId)

                // then
                res shouldBe study
            }

            it("should return null when study does not exist") {
                // when
                val res = studyService.getByUserIdAndWordDefinitionId(user!!.id!!, study!!.wordDefinitionId + 1)

                // then
                res shouldBe null
            }
        }

        describe("getById") {
            var study: Study? = null

            beforeEach {
                wordFactory.createWord()
                study = studyFactory.createStudy(userId = user!!.id!!)
            }

            it("should return study when it exists") {
                // when
                val res = studyService.getById(study!!.id!!)

                // then
                res shouldBe study
            }

            it("should return null when study does not exist") {
                // when
                val res = studyService.getById(study!!.id!! + 1)

                // then
                res shouldBe null
            }
        }

        describe("createStudy") {
            it("should raise error if study already exists") {
                // given
                val word = wordFactory.createWord()
                studyFactory.createStudy(
                    userId = user!!.id!!,
                    wordDefinitionId = word.definitions.first().id!!,
                )

                // when
                val exception =
                    assertThrows<ConflictException> {
                        studyService.createStudy(
                            StudyCreateObject(
                                userId = user!!.id!!,
                                wordDefinitionId = word.definitions.first().id!!,
                            ),
                        )
                    }

                // then
                exception.message shouldBe "Study already exists"
                exception.code shouldBe "STUDY_1"
            }

            it("should create study") {
                // given
                val word = wordFactory.createWord()
                val wordDefinitionId = word.definitions.first().id!!

                // when
                val study =
                    studyService.createStudy(
                        StudyCreateObject(
                            userId = user!!.id!!,
                            wordDefinitionId = wordDefinitionId,
                        ),
                    )

                // then
                study.userId shouldBe user!!.id!!
                study.wordDefinitionId shouldBe wordDefinitionId
            }
        }

        describe("getStudiesPastDue") {
            it("should return empty list when count is 0 or less") {
                val now = Instant.now()
                listOf(0, -1).forEach { count ->
                    // when
                    val studies = studyService.getStudiesPastDue(user!!.id!!, now, emptyList(), count)

                    // then
                    studies.isEmpty() shouldBe true
                }
            }

            it("should return list of studies") {
                // given
                val word =
                    wordFactory.createWord(
                        wordDefinitions =
                            listOf(
                                WordDefinitionCreateObject(
                                    lexicalCategory = LexicalCategoryType.NOUN,
                                    meaning = "책",
                                    preContext = "정보를 얻거나 즐거움을 얻기 위해 읽는 인쇄물",
                                ),
                                WordDefinitionCreateObject(
                                    lexicalCategory = LexicalCategoryType.VERB,
                                    meaning = "예약하다",
                                    preContext = "특정한 날짜나 시간에 무엇을 하기 위해 미리 자리를 확보하다",
                                ),
                            ),
                    )
                val studies =
                    listOf(
                        studyFactory.createStudy(userId = user!!.id!!, wordDefinitionId = word.definitions[0].id!!),
                        studyFactory.createStudy(userId = user!!.id!!, wordDefinitionId = word.definitions[1].id!!),
                    )

                // when
                val res = studyService.getStudiesPastDue(user!!.id!!, Instant.now(), emptyList(), 2)

                // then
                res shouldBe studies
            }
        }

        describe("getStudiesByIds") {
            it("should return empty list when ids is empty") {
                // when
                val studies = studyService.getStudiesByIds(emptyList())

                // then
                studies.isEmpty() shouldBe true
            }

            it("should return list of studies") {
                // given
                val word =
                    wordFactory.createWord(
                        wordDefinitions =
                            listOf(
                                WordDefinitionCreateObject(
                                    lexicalCategory = LexicalCategoryType.NOUN,
                                    meaning = "책",
                                    preContext = "정보를 얻거나 즐거움을 얻기 위해 읽는 인쇄물",
                                ),
                                WordDefinitionCreateObject(
                                    lexicalCategory = LexicalCategoryType.VERB,
                                    meaning = "예약하다",
                                    preContext = "특정한 날짜나 시간에 무엇을 하기 위해 미리 자리를 확보하다",
                                ),
                            ),
                    )
                val studies =
                    listOf(
                        studyFactory.createStudy(userId = user!!.id!!, wordDefinitionId = word.definitions[0].id!!),
                        studyFactory.createStudy(userId = user!!.id!!, wordDefinitionId = word.definitions[1].id!!),
                    )
                val ids = studies.map { it.id!! }

                // when
                val res = studyService.getStudiesByIds(ids)

                // then
                res shouldBe studies
            }
        }

        describe("getDailyStudySet") {
            it("should return daily study set when it exists") {
                // given
                val dailyStudySet = studyFactory.createDailyStudySet(user = user!!)

                // when
                val res = studyService.getDailyStudySet(user!!.id!!, LocalDate.now())

                // then
                res shouldBe dailyStudySet
            }

            it("should return null when daily study set does not exist") {
                // when
                val res = studyService.getDailyStudySet(user!!.id!!, LocalDate.now())

                // then
                res shouldBe null
            }
        }

        describe("createDailyStudySet") {
            it("should raise error if daily study set already exists") {
                // given
                studyFactory.createDailyStudySet(user = user!!)
                val dailyStudySetCreateObject =
                    DailyStudySetCreateObject(
                        userId = user!!.id!!,
                        date = LocalDate.now(),
                    )

                // when
                val exception =
                    assertThrows<ConflictException> {
                        studyService.createDailyStudySet(dailyStudySetCreateObject)
                    }

                // then
                exception.message shouldBe "Daily study set already exists"
                exception.code shouldBe "STUDY_5"
            }

            it("should create daily study set") {
                // given
                val dailyStudySetCreateObject =
                    DailyStudySetCreateObject(
                        userId = user!!.id!!,
                        date = LocalDate.now(),
                    )

                // when
                val dailyStudySet = studyService.createDailyStudySet(dailyStudySetCreateObject)

                // then
                dailyStudySetRepository.findById(dailyStudySet.id!!) shouldBe Optional.of(dailyStudySet)
                dailyStudySet.userId shouldBe user!!.id!!
                dailyStudySet.date shouldBe LocalDate.now()
            }
        }

        describe("getStudiesByDailyStudySet") {
            var word: Word?
            var studies: List<Study>? = null

            beforeEach {
                word =
                    wordFactory.createWord(
                        wordDefinitions =
                            listOf(
                                WordDefinitionCreateObject(
                                    lexicalCategory = LexicalCategoryType.NOUN,
                                    meaning = "책",
                                    preContext = "정보를 얻거나 즐거움을 얻기 위해 읽는 인쇄물",
                                ),
                                WordDefinitionCreateObject(
                                    lexicalCategory = LexicalCategoryType.VERB,
                                    meaning = "예약하다",
                                    preContext = "특정한 날짜나 시간에 무엇을 하기 위해 미리 자리를 확보하다",
                                ),
                            ),
                    )
                studies =
                    word!!.definitions.map { studyFactory.createStudy(userId = user!!.id!!, wordDefinitionId = it.id!!) }
            }

            it("should raise error if given user does not own dailyStudySet") {
                // given
                val dailyStudySet = studyFactory.createDailyStudySet(user = user!!)
                val otherUser = userFactory.createUser(email = "test2@1.com")

                // when
                val exception =
                    assertThrows<BadRequestException> {
                        studyService.getStudiesByDailyStudySet(dailyStudySet, otherUser)
                    }

                // then
                exception.message shouldBe "User does not match daily study set"
                exception.code shouldBe "STUDY_8"
            }

            it("should return daily study set studies when date is before today") {
                // given
                val userZoneId = ZoneId.of(user!!.timezone)
                val todayDate = LocalDate.now(userZoneId)
                val dailyStudySet =
                    studyFactory.createDailyStudySet(
                        user = user!!,
                        date = todayDate.minusDays(1),
                        studies = studies!!,
                    )

                // when
                val res = studyService.getStudiesByDailyStudySet(dailyStudySet, user!!)

                // then
                res.size shouldBe 2
                res shouldBe studies
            }

            it("should return daily study set studies and past due studies when date is today") {
                // given
                val dailyStudySet = studyFactory.createDailyStudySet(user = user!!, studies = studies!!)
                val pastStudiesWord =
                    wordFactory.createWord(
                        name = "booked",
                        wordDefinitions =
                            listOf(
                                WordDefinitionCreateObject(
                                    lexicalCategory = LexicalCategoryType.NOUN,
                                    meaning = "예약된",
                                    preContext = "미리 자리를 확보한",
                                ),
                            ),
                    )
                val pastDueStudies =
                    listOf(
                        studyFactory.createStudy(
                            userId = user!!.id!!,
                            wordDefinitionId = pastStudiesWord.definitions.first().id!!,
                        ),
                    )

                // when
                val res = studyService.getStudiesByDailyStudySet(dailyStudySet, user!!)

                // then
                res.size shouldBe 3
                res shouldBe studies!! + pastDueStudies
            }

            it("should return amount of studies user has set when date is today") {
                val studyPerDay = 3
                user = user!!.copy(studyPerDay = studyPerDay)
                val dailyStudySet = studyFactory.createDailyStudySet(user = user!!, studies = studies!!)
                val pastDueStudies =
                    listOf(
                        wordFactory
                            .createWord(
                                name = "booked",
                                wordDefinitions =
                                    listOf(
                                        WordDefinitionCreateObject(
                                            lexicalCategory = LexicalCategoryType.NOUN,
                                            meaning = "예약된",
                                            preContext = "미리 자리를 확보한",
                                        ),
                                    ),
                            ).let { word ->
                                studyFactory.createStudy(
                                    userId = user!!.id!!,
                                    wordDefinitionId = word.definitions.first().id!!,
                                )
                            },
                        wordFactory
                            .createWord(
                                name = "bank",
                                wordDefinitions =
                                    listOf(
                                        WordDefinitionCreateObject(
                                            lexicalCategory = LexicalCategoryType.NOUN,
                                            meaning = "은행",
                                            preContext = "돈을 보관하거나 대출을 해주는 기관",
                                        ),
                                    ),
                            ).let { word ->
                                studyFactory.createStudy(
                                    userId = user!!.id!!,
                                    wordDefinitionId = word.definitions.first().id!!,
                                )
                            },
                    )

                // when
                val res = studyService.getStudiesByDailyStudySet(dailyStudySet, user!!)

                // then
                res.size shouldBe studyPerDay
                res shouldContainAll studies!! // both studies in dailyStudySet should be included
                res shouldContainAnyOf pastDueStudies // one of the pastDueStudies should be included
            }

            it("should return studies according to user's timezone") {
                // given
                user = user!!.copy(timezone = "Asia/Seoul")
                val dailyStudySet = studyFactory.createDailyStudySet(user = user!!, studies = emptyList())
                val userZoneId = ZoneId.of(user!!.timezone)
                val nowInUserZone = Instant.now().atZone(userZoneId)
                val endOfDayInUserZone =
                    nowInUserZone
                        .toLocalDate()
                        .atTime(LocalTime.MAX)
                        .atZone(userZoneId)
                        .toInstant()
                val pastDueStudy =
                    wordFactory
                        .createWord(
                            name = "booked",
                            wordDefinitions =
                                listOf(
                                    WordDefinitionCreateObject(
                                        lexicalCategory = LexicalCategoryType.NOUN,
                                        meaning = "예약된",
                                        preContext = "미리 자리를 확보한",
                                    ),
                                ),
                        ).let { word ->
                            studyFactory.createStudy(
                                userId = user!!.id!!,
                                wordDefinitionId = word.definitions.first().id!!,
                                due = endOfDayInUserZone.minusSeconds(60),
                            )
                        }
                val beforeDueStudy =
                    wordFactory
                        .createWord(
                            name = "bank",
                            wordDefinitions =
                                listOf(
                                    WordDefinitionCreateObject(
                                        lexicalCategory = LexicalCategoryType.NOUN,
                                        meaning = "은행",
                                        preContext = "돈을 보관하거나 대출을 해주는 기관",
                                    ),
                                ),
                        ).let { word ->
                            studyFactory.createStudy(
                                userId = user!!.id!!,
                                wordDefinitionId = word.definitions.first().id!!,
                                due = endOfDayInUserZone.plusSeconds(60),
                            )
                        }

                // when
                val res = studyService.getStudiesByDailyStudySet(dailyStudySet, user!!)

                // then
                res shouldNotContain beforeDueStudy
                res shouldContain pastDueStudy
            }
        }

        describe("updateStudy") {
            it("should return rated study when study is rated") {
                // given
                val word = wordFactory.createWord()
                val study = studyFactory.createStudy(userId = user!!.id!!, wordDefinitionId = word.definitions.first().id!!)
                val rating = FsrsCardRating.EASY
                val now = Instant.now()

                // when
                val res = studyService.updateStudy(study.rate(rating))

                // then
                res.id shouldNotBe null
                res.userId shouldBe user!!.id!!
                res.wordDefinitionId shouldBe word.definitions.first().id
                res.state shouldBe FsrsCardState.REVIEW
                val expectedScheduledDays = 11
                res.scheduledDays shouldBe expectedScheduledDays
                val expectedDueRange =
                    now.plusDays(expectedScheduledDays).minusSeconds(1)..now.plusDays(expectedScheduledDays)
                res.due shouldBeIn expectedDueRange
                res.stability shouldBeGreaterThan 0.0
                res.difficulty shouldBeGreaterThan 0.0
                res.elapsedDays shouldBe 0
                res.reps shouldBe 1
                res.lapses shouldBe 0
                res.lastReview!! shouldBeIn now.minusSeconds(1)..now.plusSeconds(1)
                res.reviewLogs.size shouldBe 1
            }
        }

        describe("addStudyToDailyStudySet") {
            it("should raise error if study is already in daily study set") {
                // given
                val word = wordFactory.createWord()
                val study = studyFactory.createStudy(userId = user!!.id!!, wordDefinitionId = word.definitions.first().id!!)
                val dailyStudySet = studyFactory.createDailyStudySet(user = user!!, studies = listOf(study))

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
                val dailyStudySet = studyFactory.createDailyStudySet(user = user!!)
                val word = wordFactory.createWord()
                val study = studyFactory.createStudy(userId = user!!.id!!, wordDefinitionId = word.definitions.first().id!!)

                // when
                studyService.addStudyToDailyStudySet(dailyStudySet, study)

                // then
                val updatedDailyStudySet = dailyStudySetRepository.findById(dailyStudySet.id!!).orElseThrow()
                updatedDailyStudySet.studyIds.size shouldBe 1
                updatedDailyStudySet.studyIds.first() shouldBe study.id
            }
        }
    })

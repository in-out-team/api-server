package com.inout.apiserver.application.word

import com.inout.apiserver.base.enums.LanguageType
import com.inout.apiserver.base.enums.LexicalCategoryType
import com.inout.apiserver.domain.study.StudyFactory
import com.inout.apiserver.domain.user.UserFactory
import com.inout.apiserver.domain.word.WordFactory
import com.inout.apiserver.extension.cleanUp
import com.inout.apiserver.helper.InOutSpringBootTest
import com.inout.apiserver.infrastructure.db.user.User
import com.inout.apiserver.infrastructure.db.word.Word
import com.inout.apiserver.infrastructure.db.word.WordDefinition
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import org.springframework.data.domain.PageRequest
import org.springframework.jdbc.core.JdbcTemplate

@InOutSpringBootTest
class ReadWordsApplicationTest(
    private val subject: ReadWordsApplication,
    // factories
    private val userFactory: UserFactory,
    private val wordFactory: WordFactory,
    private val studyFactory: StudyFactory,
    // etc
    private val jdbcTemplate: JdbcTemplate,
) : DescribeSpec({
        afterEach {
            jdbcTemplate.cleanUp()
        }

        describe("when matching word does not exist") {
            it("should return empty list") {
                // given
                val fromLanguage = LanguageType.ENGLISH
                val toLanguage = LanguageType.KOREAN
                val prefix = "book"
                val lexicalCategoryType = null
                val pageable = PageRequest.of(0, 1)

                // when
                val (totalCount, content, studyingWordDefinitionIds) =
                    subject.run(
                        ReadWordsApplication.Request(
                            fromLanguage = fromLanguage,
                            toLanguage = toLanguage,
                            prefix = prefix,
                            lexicalCategoryType = lexicalCategoryType,
                            pageable = pageable,
                            user = userFactory.createUser(),
                        ),
                    )

                // then
                totalCount shouldBe 0
                content.size shouldBe 0
                studyingWordDefinitionIds.size shouldBe 0
            }
        }

        describe("when matching word exists") {
            var user: User? = null
            var words: MutableList<Word>? = null

            beforeEach {
                user = userFactory.createUser()
                words = mutableListOf()
                val word1 =
                    wordFactory.createWord(
                        name = "board",
                        wordDefinitions =
                            listOf(
                                WordDefinition(
                                    lexicalCategory = LexicalCategoryType.NOUN,
                                    meaning = "판자",
                                    preContext = "무엇을 올리거나 붙이기 위해 사용되는 넓고 평평한 나무 조각",
                                ),
                                WordDefinition(
                                    lexicalCategory = LexicalCategoryType.VERB,
                                    meaning = "탑승하다",
                                    preContext = "특정한 교통 수단에 몸을 올리다",
                                ),
                                WordDefinition(
                                    lexicalCategory = LexicalCategoryType.ADJECTIVE,
                                    meaning = "공공의",
                                    preContext = "공공의 기관이나 단체에 속한",
                                ),
                            ),
                    )
                words!!.add(word1)
                val word2 =
                    wordFactory.createWord(
                        name = "boast",
                        wordDefinitions =
                            listOf(
                                WordDefinition(
                                    lexicalCategory = LexicalCategoryType.VERB,
                                    meaning = "자랑하다",
                                    preContext = "자신의 능력이나 성과를 자랑스럽게 말하다",
                                ),
                                WordDefinition(
                                    lexicalCategory = LexicalCategoryType.NOUN,
                                    meaning = "자랑",
                                    preContext = "자신의 능력이나 성과를 자랑스럽게 말함",
                                ),
                            ),
                    )
                words!!.add(word2)
                val word3 =
                    wordFactory.createWord(
                        name = "book",
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
                words!!.add(word3)
            }

            it("should return words with matching prefix") {
                // given
                val fromLanguage = LanguageType.ENGLISH
                val toLanguage = LanguageType.KOREAN
                val prefix = "boa"
                val lexicalCategoryType = null
                val pageable = PageRequest.of(0, 5)

                // when
                val (totalCount, content, studyingWordDefinitionIds) =
                    subject.run(
                        ReadWordsApplication.Request(
                            fromLanguage = fromLanguage,
                            toLanguage = toLanguage,
                            prefix = prefix,
                            lexicalCategoryType = lexicalCategoryType,
                            pageable = pageable,
                            user = user!!,
                        ),
                    )

                // then
                totalCount shouldBe 2
                content.size shouldBe 2
                content.map { it.name } shouldNotContain "book"
                studyingWordDefinitionIds.size shouldBe 0
            }

            it("should return words with matching prefix and lexical category") {
                // given
                val fromLanguage = LanguageType.ENGLISH
                val toLanguage = LanguageType.KOREAN
                val prefix = "boa"
                val lexicalCategoryType = LexicalCategoryType.ADJECTIVE
                val pageable = PageRequest.of(0, 5)

                // when
                val (totalCount, content, studyingWordDefinitionIds) =
                    subject.run(
                        ReadWordsApplication.Request(
                            fromLanguage = fromLanguage,
                            toLanguage = toLanguage,
                            prefix = prefix,
                            lexicalCategoryType = lexicalCategoryType,
                            pageable = pageable,
                            user = user!!,
                        ),
                    )

                // then
                totalCount shouldBe 1
                content.size shouldBe 1
                studyingWordDefinitionIds.size shouldBe 0
            }

            it("should return words with matching prefix and user's studying word definition ids") {
                // given
                val fromLanguage = LanguageType.ENGLISH
                val toLanguage = LanguageType.KOREAN
                val prefix = "boa"
                val lexicalCategoryType = null
                val pageable = PageRequest.of(0, 5)
                val studyingWordDefinitionIds = words!!.map { it.definitions.last().id!! }
                studyingWordDefinitionIds.forEach { wordDefinitionId ->
                    studyFactory.createStudy(
                        userId = user!!.id!!,
                        wordDefinitionId = wordDefinitionId,
                    )
                }

                // when
                val (totalCount, content, returnedStudyingWordDefinitionIds) =
                    subject.run(
                        ReadWordsApplication.Request(
                            fromLanguage = fromLanguage,
                            toLanguage = toLanguage,
                            prefix = prefix,
                            lexicalCategoryType = lexicalCategoryType,
                            pageable = pageable,
                            user = user!!,
                        ),
                    )

                // then
                totalCount shouldBe 2
                content.size shouldBe 2
                returnedStudyingWordDefinitionIds.size shouldBe 2
                returnedStudyingWordDefinitionIds shouldBe
                    words!!.mapNotNull { word ->
                        if (word.name.startsWith(prefix)) {
                            word.definitions.last().id
                        } else {
                            null
                        }
                    }
            }
        }
    })

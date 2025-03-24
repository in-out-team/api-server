package com.inout.apiserver.application.study

import com.inout.apiserver.base.enums.LexicalCategoryType
import com.inout.apiserver.domain.study.StudyFactory
import com.inout.apiserver.domain.user.UserFactory
import com.inout.apiserver.domain.word.WordDefinitionCreateObject
import com.inout.apiserver.domain.word.WordFactory
import com.inout.apiserver.extension.cleanUp
import com.inout.apiserver.helper.InOutSpringBootTest
import com.inout.apiserver.infrastructure.db.user.User
import com.inout.apiserver.infrastructure.db.word.Word
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.springframework.data.domain.PageRequest
import org.springframework.jdbc.core.JdbcTemplate

@InOutSpringBootTest
class ReadStudiesApplicationTest(
    private val subject: ReadStudiesApplication,
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

        describe("run") {
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
                                WordDefinitionCreateObject(
                                    lexicalCategory = LexicalCategoryType.NOUN,
                                    meaning = "판자",
                                    preContext = "무엇을 올리거나 붙이기 위해 사용되는 넓고 평평한 나무 조각",
                                ),
                                WordDefinitionCreateObject(
                                    lexicalCategory = LexicalCategoryType.VERB,
                                    meaning = "탑승하다",
                                    preContext = "특정한 교통 수단에 몸을 올리다",
                                ),
                                WordDefinitionCreateObject(
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
                                WordDefinitionCreateObject(
                                    lexicalCategory = LexicalCategoryType.VERB,
                                    meaning = "자랑하다",
                                    preContext = "자신의 능력이나 성과를 자랑스럽게 말하다",
                                ),
                                WordDefinitionCreateObject(
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
                words!!.add(word3)
            }

            it("should return studies") {
                // given
                val targetWordDefinitionIds = words!!.map { it.definitions.first().id!! }
                val studies =
                    targetWordDefinitionIds.map { studyFactory.createStudy(userId = user!!.id!!, wordDefinitionId = it) }
                val pageable = PageRequest.of(0, studies.size - 1)

                // when
                val result = subject.run(ReadStudiesApplication.Request(userId = user!!.id!!, pageable = pageable))

                // then
                result.totalCount shouldBe studies.size.toLong()
                result.studies.size shouldBe studies.size - 1
                result.studies.forEachIndexed { index, studyWord ->
                    studyWord.study shouldBe studies[index]
                    studyWord.word.id shouldBe words!![index].id
                    studyWord.word.name shouldBe words!![index].name
                    studyWord.word.definitions.size shouldBe 1
                    studyWord.word.definitions
                        .first()
                        .id shouldBe studyWord.study.wordDefinitionId
                }
            }
        }
    })

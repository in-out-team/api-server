package com.inout.apiserver.application.study

import com.inout.apiserver.base.enums.LexicalCategoryType
import com.inout.apiserver.base.enums.StatusType
import com.inout.apiserver.domain.study.StudyFactory
import com.inout.apiserver.domain.user.UserFactory
import com.inout.apiserver.domain.word.WordFactory
import com.inout.apiserver.domain.word.WordWithDefinitions
import com.inout.apiserver.extension.cleanUp
import com.inout.apiserver.helper.InOutSpringBootTest
import com.inout.apiserver.infrastructure.mongo.user.User
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeSortedBy
import io.kotest.matchers.shouldBe
import org.springframework.data.domain.PageRequest
import org.springframework.data.mongodb.core.MongoTemplate

@InOutSpringBootTest
class ReadStudiesApplicationTest(
    private val subject: ReadStudiesApplication,
    // factories
    private val userFactory: UserFactory,
    private val wordFactory: WordFactory,
    private val studyFactory: StudyFactory,
    // etc
    private val mongoTemplate: MongoTemplate,
) : DescribeSpec({
        afterEach {
            mongoTemplate.cleanUp()
        }

        describe("run") {
            var user: User? = null
            var words: MutableList<WordWithDefinitions>? = null

            beforeEach {
                user = userFactory.createUser()
                words = mutableListOf()
                val word1 =
                    wordFactory.createWord(
                        name = "board",
                        definitions =
                            listOf(
                                WordWithDefinitions.WordDefinition(
                                    lexicalCategory = LexicalCategoryType.NOUN,
                                    meaning = "판자",
                                    preContext = "무엇을 올리거나 붙이기 위해 사용되는 넓고 평평한 나무 조각",
                                    status = StatusType.LIVE,
                                ),
                                WordWithDefinitions.WordDefinition(
                                    lexicalCategory = LexicalCategoryType.VERB,
                                    meaning = "탑승하다",
                                    preContext = "특정한 교통 수단에 몸을 올리다",
                                    status = StatusType.LIVE,
                                ),
                                WordWithDefinitions.WordDefinition(
                                    lexicalCategory = LexicalCategoryType.ADJECTIVE,
                                    meaning = "공공의",
                                    preContext = "공공의 기관이나 단체에 속한",
                                    status = StatusType.LIVE,
                                ),
                            ),
                    )
                words!!.add(word1)
                val word2 =
                    wordFactory.createWord(
                        name = "boast",
                        definitions =
                            listOf(
                                WordWithDefinitions.WordDefinition(
                                    lexicalCategory = LexicalCategoryType.VERB,
                                    meaning = "자랑하다",
                                    preContext = "자신의 능력이나 성과를 자랑스럽게 말하다",
                                    status = StatusType.LIVE,
                                ),
                                WordWithDefinitions.WordDefinition(
                                    lexicalCategory = LexicalCategoryType.NOUN,
                                    meaning = "자랑",
                                    preContext = "자신의 능력이나 성과를 자랑스럽게 말함",
                                    status = StatusType.LIVE,
                                ),
                            ),
                    )
                words!!.add(word2)
                val word3 =
                    wordFactory.createWord(
                        name = "book",
                        definitions =
                            listOf(
                                WordWithDefinitions.WordDefinition(
                                    lexicalCategory = LexicalCategoryType.NOUN,
                                    meaning = "책",
                                    preContext = "정보를 얻거나 즐거움을 얻기 위해 읽는 인쇄물",
                                    status = StatusType.LIVE,
                                ),
                                WordWithDefinitions.WordDefinition(
                                    lexicalCategory = LexicalCategoryType.VERB,
                                    meaning = "예약하다",
                                    preContext = "특정한 날짜나 시간에 무엇을 하기 위해 미리 자리를 확보하다",
                                    status = StatusType.LIVE,
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
                val result =
                    subject.run(
                        ReadStudiesApplication.Request(
                            user = user!!,
                            wordNamePrefix = null,
                            pageable = pageable,
                        ),
                    )

                // then
                result.totalCount shouldBe studies.size.toLong()
                result.studies.size shouldBe studies.size - 1
                result.studies shouldBeSortedBy { it.study.due }
            }
        }
    })

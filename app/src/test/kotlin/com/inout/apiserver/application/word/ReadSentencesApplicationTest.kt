package com.inout.apiserver.application.word

import com.inout.apiserver.base.enums.LanguageType
import com.inout.apiserver.base.enums.LexicalCategoryType
import com.inout.apiserver.domain.user.User
import com.inout.apiserver.domain.user.UserFactory
import com.inout.apiserver.domain.word.Sentence
import com.inout.apiserver.domain.word.UserSentenceCreateObject
import com.inout.apiserver.domain.word.Word
import com.inout.apiserver.domain.word.WordFactory
import com.inout.apiserver.domain.word.WordService
import com.inout.apiserver.error.NotFoundException
import com.inout.apiserver.extension.cleanUp
import com.inout.apiserver.helper.InOutSpringBootTest
import com.inout.apiserver.infrastructure.db.word.WordDefinitionEntity
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.assertThrows
import org.springframework.jdbc.core.JdbcTemplate

@InOutSpringBootTest
class ReadSentencesApplicationTest(
    // services
    private val wordService: WordService,
    // factories
    private val userFactory: UserFactory,
    private val wordFactory: WordFactory,
    // etc
    private val jdbcTemplate: JdbcTemplate,
) : DescribeSpec({
        var user: User? = null
        var word: Word? = null

        beforeEach {
            user = userFactory.createUser()
            word =
                wordFactory.createWord(
                    name = "book",
                    fromLanguage = LanguageType.ENGLISH,
                    toLanguage = LanguageType.KOREAN,
                    wordDefinitions =
                        listOf(
                            WordDefinitionEntity(
                                lexicalCategory = LexicalCategoryType.NOUN,
                                meaning = "책",
                                preContext = "정보를 얻거나 즐거움을 얻기 위해 읽는 인쇄물",
                            ),
                            WordDefinitionEntity(
                                lexicalCategory = LexicalCategoryType.VERB,
                                meaning = "예약하다",
                                preContext = "특정한 날짜나 시간에 미리 자리를 확보하다",
                            ),
                        ),
                )
        }

        afterEach {
            jdbcTemplate.cleanUp()
        }

        describe("when sentences not found") {
            it("should raise error") {
                // given
                val wordDefinitionId = 1L

                // when
                val exception =
                    assertThrows<NotFoundException> {
                        ReadSentencesApplication(wordService).run(wordDefinitionId, user!!)
                    }

                // then
                exception.message shouldBe "Sentences not found for word definition id: $wordDefinitionId"
                exception.code shouldBe "WORD_3"
            }
        }

        describe("when sentences found") {
            var sentences: List<Sentence>? = null

            beforeEach {
                sentences =
                    word!!.definitions.map {
                        wordFactory.createSentence(it.id, "sentence content", "sentence translation")
                    }
            }

            it("should return sentences") {
                // given
                val wordDefinitionId = word!!.definitions.first().id

                // when
                val result = ReadSentencesApplication(wordService).run(wordDefinitionId, user!!)

                // then
                result.first.size shouldBe 0
                result.second.size shouldBe 2
            }

            it("should return selected sentences as first") {
                // given
                val wordDefinitionId = word!!.definitions.first().id
                val selectedSentence = sentences!!.first()
                wordService.createUserSentence(
                    UserSentenceCreateObject(
                        userId = user!!.id,
                        wordDefinitionId = word!!.definitions.first().id,
                        sentenceId = selectedSentence.id,
                    ),
                )

                // when
                val result = ReadSentencesApplication(wordService).run(wordDefinitionId, user!!)

                // then
                result.first.size shouldBe 1
                result.first.first().id shouldBe selectedSentence.id
                result.second.size shouldBe 1
                result.second.first().id shouldBe sentences!!.last().id
            }
        }
    })

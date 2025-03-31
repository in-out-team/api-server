package com.inout.apiserver.application.word.admin

import com.inout.apiserver.base.enums.LexicalCategoryType
import com.inout.apiserver.base.enums.StatusType
import com.inout.apiserver.domain.word.WordFactory
import com.inout.apiserver.error.BadRequestException
import com.inout.apiserver.error.NotFoundException
import com.inout.apiserver.extension.cleanUp
import com.inout.apiserver.helper.InOutSpringBootTest
import com.inout.apiserver.infrastructure.db.word.Word
import com.inout.apiserver.infrastructure.db.word.WordDefinition
import com.inout.apiserver.infrastructure.db.word.WordRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.springframework.jdbc.core.JdbcTemplate

@InOutSpringBootTest
class ApproveWordDefinitionApplicationTest(
    private val subject: ApproveWordDefinitionApplication,
    // factories
    private val wordFactory: WordFactory,
    // repositories
    private val wordRepository: WordRepository,
    // etc
    private val jdbcTemplate: JdbcTemplate,
) : DescribeSpec({
        var word: Word? = null
        var wordDefinitionId = 0L

        beforeEach {
            word = wordFactory.createWord()
            wordDefinitionId = word!!.definitions.first().id!!
        }

        afterEach {
            jdbcTemplate.cleanUp()
        }

        describe("when wordDefinitionId of wordId does not exist") {
            it("should throw NotFoundException") {
                // given
                val requests =
                    listOf(
                        ApproveWordDefinitionApplication.Request(
                            wordId = word!!.id!!,
                            wordDefinitionId = 9999L,
                        ),
                        ApproveWordDefinitionApplication.Request(
                            wordId = 9999L,
                            wordDefinitionId = wordDefinitionId,
                        ),
                    )

                // when & then
                requests.forEach { request ->
                    val exception = shouldThrow<NotFoundException> { subject.run(request) }

                    exception.message shouldBe "Word definition not found"
                    exception.code shouldBe "WORD_4"
                }
            }
        }

        describe("when wordId and wordDefinitionId exists but is not pending") {
            it("should throw BadRequestException") {
                // given
                val request =
                    ApproveWordDefinitionApplication.Request(
                        wordId = word!!.id!!,
                        wordDefinitionId = wordDefinitionId,
                    )

                // when
                val exception = shouldThrow<BadRequestException> { subject.run(request) }

                // then
                exception.message shouldBe "Cannot approve word definition that is not pending"
                exception.code shouldBe "WORD_6"
            }
        }

        describe("when wordId and wordDefinitionId exists and is pending") {
            it("should approve word definition") {
                // given
                word!!.addDefinitions(
                    listOf(
                        WordDefinition(
                            wordId = word!!.id!!,
                            lexicalCategory = LexicalCategoryType.VERB,
                            meaning = "예약하다",
                            preContext = "특정한 날짜나 시간에 무엇을 하기 위해 미리 자리를 확보하다",
                        ),
                    ),
                )
                val updatedWord = wordRepository.save(word!!)
                wordDefinitionId = updatedWord.definitions.last().id!!
                updatedWord.definitions.last().status shouldBe StatusType.PENDING
                val request =
                    ApproveWordDefinitionApplication.Request(
                        wordId = updatedWord.id!!,
                        wordDefinitionId = wordDefinitionId,
                    )

                // when
                val response = subject.run(request)

                // then
                response.word.id shouldBe updatedWord.id
                response.word.definitions.size shouldBe updatedWord.definitions.size
                response.word.definitions
                    .last()
                    .id shouldBe wordDefinitionId
                response.word.definitions
                    .last()
                    .status shouldBe StatusType.LIVE
            }
        }
    })

package com.inout.apiserver.application.word.admin

import com.inout.apiserver.base.enums.LexicalCategoryType
import com.inout.apiserver.base.enums.StatusType
import com.inout.apiserver.domain.word.WordFactory
import com.inout.apiserver.error.ConflictException
import com.inout.apiserver.error.NotFoundException
import com.inout.apiserver.extension.cleanUp
import com.inout.apiserver.helper.InOutSpringBootTest
import com.inout.apiserver.infrastructure.db.word.Word
import com.inout.apiserver.infrastructure.db.word.WordRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.springframework.jdbc.core.JdbcTemplate

@InOutSpringBootTest
class AddWordDefinitionApplicationTest(
    private val subject: AddWordDefinitionApplication,
    // factories
    private val wordFactory: WordFactory,
    // repositories
    private val wordRepository: WordRepository,
    // etc
    private val jdbcTemplate: JdbcTemplate,
) : DescribeSpec({
        afterEach {
            jdbcTemplate.cleanUp()
        }

        describe("when word does not exist") {
            it("should throw NotFoundException") {
                // given
                val request =
                    AddWordDefinitionApplication.Request(
                        wordId = 1L,
                        lexicalCategory = LexicalCategoryType.NOUN,
                        meaning = "책",
                        preContext = "정보를 얻거나 즐거움을 얻기 위해 읽는 인쇄물",
                    )

                // when
                val exception = shouldThrow<NotFoundException> { subject.run(request) }

                // then
                exception.message shouldBe "Word not found"
                exception.code shouldBe "WORD_4"
            }
        }

        describe("when word exists") {
            var word: Word? = null

            beforeEach {
                word = wordFactory.createWord()
            }

            it("should throw ConflictException when definition already exists") {
                // given
                val request =
                    AddWordDefinitionApplication.Request(
                        wordId = word!!.id!!,
                        lexicalCategory = LexicalCategoryType.NOUN,
                        meaning = "책",
                        preContext = "정보를 얻거나 즐거움을 얻기 위해 읽는 인쇄물",
                    )

                // when
                val exception = shouldThrow<ConflictException> { subject.run(request) }

                // then
                exception.message shouldBe "Word definition already exists"
                exception.code shouldBe "WORD_5"
            }

            it("should add definition to word") {
                // given
                val request =
                    AddWordDefinitionApplication.Request(
                        wordId = word!!.id!!,
                        lexicalCategory = LexicalCategoryType.VERB,
                        meaning = "예약하다",
                        preContext = "특정한 날짜나 시간에 무엇을 하기 위해 미리 자리를 확보하다",
                    )

                // when
                val response = subject.run(request)

                // then
                response.word.definitions.size shouldBe 2
                response.word.definitions[1].lexicalCategory shouldBe request.lexicalCategory
                response.word.definitions[1].meaning shouldBe request.meaning
                response.word.definitions[1].preContext shouldBe request.preContext
                response.word.definitions[1].status shouldBe StatusType.PENDING
            }

            it("should add definition to word if conflict definition is removed") {
                // given
                word!!.removeDefinition(wordDefinitionId = word!!.definitions.first().id!!)
                word = wordRepository.save(word!!)
                val request =
                    AddWordDefinitionApplication.Request(
                        wordId = word!!.id!!,
                        lexicalCategory = LexicalCategoryType.NOUN,
                        meaning = "책",
                        preContext = "정보를 얻거나 즐거움을 얻기 위해 읽는 인쇄물",
                    )

                // when
                val response = subject.run(request)

                // then
                response.word.definitions.size shouldBe 2
                response.word.definitions[1].lexicalCategory shouldBe request.lexicalCategory
                response.word.definitions[1].meaning shouldBe request.meaning
                response.word.definitions[1].preContext shouldBe request.preContext
                response.word.definitions[1].status shouldBe StatusType.PENDING
            }
        }
    })

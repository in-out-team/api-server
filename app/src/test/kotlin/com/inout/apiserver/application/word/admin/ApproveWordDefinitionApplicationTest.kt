package com.inout.apiserver.application.word.admin

import com.inout.apiserver.base.enums.LexicalCategoryType
import com.inout.apiserver.base.enums.StatusType
import com.inout.apiserver.domain.word.MongoWordFactory
import com.inout.apiserver.domain.word.MongoWordService
import com.inout.apiserver.domain.word.WordWithDefinitions
import com.inout.apiserver.error.BadRequestException
import com.inout.apiserver.error.NotFoundException
import com.inout.apiserver.extension.cleanUp
import com.inout.apiserver.helper.InOutSpringBootTest
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.MongoTemplate

@InOutSpringBootTest
class ApproveWordDefinitionApplicationTest(
    private val subject: ApproveWordDefinitionApplication,
    // factories
    private val wordFactory: MongoWordFactory,
    // services
    private val wordService: MongoWordService,
    // etc
    private val mongoTemplate: MongoTemplate,
) : DescribeSpec({
        var word: WordWithDefinitions? = null
        var wordDefinitionId = ObjectId()

        beforeEach {
            word = wordFactory.createWord()
            wordDefinitionId = word!!.definitions.first().id!!
        }

        afterEach {
            mongoTemplate.cleanUp()
        }

        describe("when wordDefinitionId of wordId does not exist") {
            it("should throw NotFoundException") {
                // given
                val requests =
                    listOf(
                        ApproveWordDefinitionApplication.Request(
                            wordId = word!!.id!!,
                            wordDefinitionId = ObjectId(),
                        ),
                        ApproveWordDefinitionApplication.Request(
                            wordId = ObjectId(),
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
                val updatedWord =
                    wordService.addWordDefinition(
                        word = word!!,
                        lexicalCategory = LexicalCategoryType.VERB,
                        meaning = "예약하다",
                        preContext = "특정한 날짜나 시간에 무엇을 하기 위해 미리 자리를 확보하다",
                    )

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

package com.inout.apiserver.application.word.admin

import com.inout.apiserver.base.enums.LexicalCategoryType
import com.inout.apiserver.base.enums.StatusType
import com.inout.apiserver.domain.study.MongoStudyFactory
import com.inout.apiserver.domain.user.MongoUserFactory
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
class DeleteWordDefinitionApplicationTest(
    private val subject: DeleteWordDefinitionApplication,
    // factories
    private val wordFactory: MongoWordFactory,
    private val userFactory: MongoUserFactory,
    private val studyFactory: MongoStudyFactory,
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
                        DeleteWordDefinitionApplication.Request(
                            wordId = word!!.id!!,
                            wordDefinitionId = ObjectId(),
                        ),
                        DeleteWordDefinitionApplication.Request(
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

        describe("when wordDefinitionId of wordId exists") {
            it("should throw BadRequestException if already deleted") {
                // given
                wordService.removeWordDefinition(word = word!!, wordDefinitionId = wordDefinitionId)

                val request =
                    DeleteWordDefinitionApplication.Request(
                        wordId = word!!.id!!,
                        wordDefinitionId = wordDefinitionId,
                    )

                // when & then
                val exception = shouldThrow<BadRequestException> { subject.run(request) }

                exception.message shouldBe "Cannot delete word definition that is not pending or not used"
                exception.code shouldBe "WORD_7"
            }

            it("should throw BadRequestException if already used") {
                // given
                val user = userFactory.createUser()
                studyFactory.createStudy(userId = user.id!!, wordDefinitionId = wordDefinitionId)

                val request =
                    DeleteWordDefinitionApplication.Request(
                        wordId = word!!.id!!,
                        wordDefinitionId = wordDefinitionId,
                    )

                // when & then
                val exception = shouldThrow<BadRequestException> { subject.run(request) }

                exception.message shouldBe "Cannot delete word definition that is not pending or not used"
                exception.code shouldBe "WORD_7"
            }

            it("should delete word definition") {
                // scenario 1. deleting live word definition with no one studying it
                // given
                val request =
                    DeleteWordDefinitionApplication.Request(
                        wordId = word!!.id!!,
                        wordDefinitionId = wordDefinitionId,
                    )

                // when
                val response = subject.run(request)

                // then
                response.word.id shouldBe word!!.id
                response.word.definitions.size shouldBe word!!.definitions.size
                response.word.definitions
                    .find { it.id == wordDefinitionId }!!
                    .let { it.status shouldBe StatusType.REMOVED }

                // scenario 2. deleting pending word definition
                // given
                word = wordService.getWordWithDefinitionsBy(word!!.id!!)
                val updatedWord =
                    wordService.addWordDefinition(
                        word = word!!,
                        lexicalCategory = LexicalCategoryType.VERB,
                        meaning = "예약하다",
                        preContext = "특정한 날짜나 시간에 무엇을 하기 위해 미리 자리를 확보하다",
                    )
                wordDefinitionId = updatedWord.definitions.find { it.status == StatusType.PENDING }!!.id!!

                val request2 =
                    DeleteWordDefinitionApplication.Request(
                        wordId = updatedWord.id!!,
                        wordDefinitionId = wordDefinitionId,
                    )

                // when
                val response2 = subject.run(request2)

                // then
                response2.word.id shouldBe updatedWord.id
                response2.word.definitions.size shouldBe updatedWord.definitions.size
                response2.word.definitions
                    .find { it.id == wordDefinitionId }!!
                    .let { it.status shouldBe StatusType.REMOVED }
            }
        }
    })

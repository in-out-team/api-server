package com.inout.apiserver.application.word

import com.inout.apiserver.base.enums.SentenceType
import com.inout.apiserver.domain.user.UserFactory
import com.inout.apiserver.domain.word.WordFactory
import com.inout.apiserver.domain.word.WordService
import com.inout.apiserver.domain.word.WordWithDefinitions
import com.inout.apiserver.error.NotFoundException
import com.inout.apiserver.extension.cleanUp
import com.inout.apiserver.helper.InOutSpringBootTest
import com.inout.apiserver.infrastructure.mongo.user.User
import com.inout.apiserver.infrastructure.mongo.word.Sentence
import com.inout.apiserver.infrastructure.mongo.word.UserSentenceRepository
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.assertThrows
import org.springframework.data.mongodb.core.MongoTemplate

@InOutSpringBootTest
class GetReadingSentencesApplicationTest(
    private val getReadingSentencesApplication: GetReadingSentencesApplication,
    // repositories
    private val userSentenceRepository: UserSentenceRepository,
    // services
    private val wordService: WordService,
    // factories
    private val userFactory: UserFactory,
    private val wordFactory: WordFactory,
    // etc
    private val mongoTemplate: MongoTemplate,
) : DescribeSpec({
        var user: User? = null
        var word: WordWithDefinitions? = null

        beforeEach {
            user = userFactory.createUser()
            word = wordFactory.createWord()
        }

        afterEach {
            mongoTemplate.cleanUp()
        }

        describe("when sentences not found") {
            it("should raise error") {
                // given
                val wordDefinitionId = word!!.definitions.first().id!!

                // when
                val exception =
                    assertThrows<NotFoundException> {
                        GetReadingSentencesApplication(wordService).run(
                            GetReadingSentencesApplication.Request(
                                wordDefinitionId = wordDefinitionId,
                                user = user!!,
                            ),
                        )
                    }

                // then
                exception.message shouldBe "Sentences not found for word definition id: $wordDefinitionId"
                exception.code shouldBe "SENTENCE_1"
            }
        }

        describe("when sentences found") {
            var sentences: List<Sentence>? = null

            beforeEach {
                val wordDefinition = word!!.definitions.first()
                sentences =
                    listOf(
                        wordFactory.createSentence(wordDefinition.id!!, "sentence1", "sentence1 translation"),
                        wordFactory.createSentence(wordDefinition.id!!, "sentence2", "sentence2 translation"),
                    )
            }

            it("should choose sentences if no previous selection exists") {
                // given
                val wordDefinitionId = word!!.definitions.first().id!!
                userSentenceRepository
                    .findAllByUserIdAndWordDefinitionIdAndType(
                        user!!.id!!,
                        wordDefinitionId,
                        SentenceType.READING,
                    ).size shouldBe 0

                // when
                val result =
                    getReadingSentencesApplication.run(
                        GetReadingSentencesApplication.Request(
                            wordDefinitionId = wordDefinitionId,
                            user = user!!,
                        ),
                    )

                // then
                result.selectedSentences.size shouldBeGreaterThan 0
            }

            it("should return selected sentences as first") {
                // given
                val wordDefinitionId = word!!.definitions.first().id!!
                val selectedSentence = sentences!!.first()
                wordService.createUserSentence(
                    userId = user!!.id!!,
                    wordDefinitionId = wordDefinitionId,
                    type = SentenceType.READING,
                    sentenceId = selectedSentence.id!!,
                )

                // when
                val result =
                    getReadingSentencesApplication.run(
                        GetReadingSentencesApplication.Request(
                            wordDefinitionId = wordDefinitionId,
                            user = user!!,
                        ),
                    )

                // then
                result.selectedSentences.size shouldBe 1
                result.selectedSentences.first().id shouldBe selectedSentence.id
                result.unselectedSentences.size shouldBe 1
                result.unselectedSentences.first().id shouldBe sentences!!.last().id
            }
        }
    })

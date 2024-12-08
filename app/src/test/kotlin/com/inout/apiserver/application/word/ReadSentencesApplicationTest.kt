package com.inout.apiserver.application.word

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
import com.inout.apiserver.infrastructure.db.word.UserSentenceRepository
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.assertThrows
import org.springframework.jdbc.core.JdbcTemplate

@InOutSpringBootTest
class ReadSentencesApplicationTest(
    private val readSentencesApplication: ReadSentencesApplication,
    // repositories
    private val userSentenceRepository: UserSentenceRepository,
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
            word = wordFactory.createWord()
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
                val wordDefinition = word!!.definitions.first()
                sentences =
                    listOf(
                        wordFactory.createSentence(wordDefinition.id, "sentence1", "sentence1 translation"),
                        wordFactory.createSentence(wordDefinition.id, "sentence2", "sentence2 translation"),
                    )
            }

            it("should choose sentences if no previous selection exists") {
                // given
                val wordDefinitionId = word!!.definitions.first().id
                userSentenceRepository.findAllByUserIdAndWordDefinitionId(user!!.id, wordDefinitionId).size shouldBe 0

                // when
                val result = readSentencesApplication.run(wordDefinitionId, user!!)

                // then
                result.first.size shouldBeGreaterThan 0
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
                val result = readSentencesApplication.run(wordDefinitionId, user!!)

                // then
                result.first.size shouldBe 1
                result.first.first().id shouldBe selectedSentence.id
                result.second.size shouldBe 1
                result.second.first().id shouldBe sentences!!.last().id
            }
        }
    })

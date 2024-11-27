package com.inout.apiserver.application.word

import com.inout.apiserver.domain.word.SentenceCreateObject
import com.inout.apiserver.domain.word.WordFactory
import com.inout.apiserver.domain.word.WordService
import com.inout.apiserver.error.NotFoundException
import com.inout.apiserver.extension.cleanUp
import com.inout.apiserver.helper.InOutSpringBootTest
import com.inout.apiserver.infrastructure.db.word.SentenceEntity
import com.inout.apiserver.infrastructure.db.word.SentenceRepository
import com.inout.apiserver.infrastructure.db.word.WordEntity
import com.inout.apiserver.infrastructure.db.word.WordRepository
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.assertThrows
import org.springframework.jdbc.core.JdbcTemplate

@InOutSpringBootTest
class ReadSentencesApplicationTest(
    // services
    private val wordService: WordService,
    // repositories
    private val sentenceRepository: SentenceRepository,
    private val wordRepository: WordRepository,
    // etc
    private val jdbcTemplate: JdbcTemplate,
) : DescribeSpec({
        fun createWord() = wordRepository.save(WordEntity.fromDomain(WordFactory.createWord()))

        fun createSentence(wordDefinitionId: Long) =
            sentenceRepository.save(
                SentenceEntity.fromCreateObject(
                    SentenceCreateObject(
                        wordDefinitionId = wordDefinitionId,
                        content = "sentence content",
                        translation = "sentence translation",
                    ),
                ),
            )

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
                        ReadSentencesApplication(wordService).run(wordDefinitionId)
                    }

                // then
                exception.message shouldBe "Sentences not found for word definition id: $wordDefinitionId"
                exception.code shouldBe "WORD_3"
            }
        }

        describe("when sentences found") {
            it("should return sentences") {
                // given
                val word = createWord()
                val wordDefinitionId = word.definitions.first().id
                val sentence = createSentence(wordDefinitionId)

                // when
                val result = ReadSentencesApplication(wordService).run(wordDefinitionId)

                // then
                result.size shouldBe 1
                val resultSentence = result.first()
                sentence.id shouldBe resultSentence.id
                wordDefinitionId shouldBe resultSentence.wordDefinitionId
                "sentence content" shouldBe resultSentence.content
            }
        }
    })

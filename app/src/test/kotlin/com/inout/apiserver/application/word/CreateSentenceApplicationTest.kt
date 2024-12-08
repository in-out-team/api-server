package com.inout.apiserver.application.word

import com.inout.apiserver.base.service.openai.OpenAIService
import com.inout.apiserver.base.service.openai.dto.OpenAIWordDefinitionSentenceResponse
import com.inout.apiserver.base.service.openai.dto.Sentence
import com.inout.apiserver.domain.word.WordFactory
import com.inout.apiserver.domain.word.WordService
import com.inout.apiserver.error.NotFoundException
import com.inout.apiserver.extension.cleanUp
import com.inout.apiserver.helper.InOutSpringBootTest
import com.inout.apiserver.infrastructure.db.word.WordEntity
import com.inout.apiserver.infrastructure.db.word.WordRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.jdbc.core.JdbcTemplate

@InOutSpringBootTest
class CreateSentenceApplicationTest(
    private val createSentenceApplication: CreateSentenceApplication,
    private val wordRepository: WordRepository,
    @SpyBean
    private val openAIService: OpenAIService,
    private val wordService: WordService,
    // etc
    private val jdbcTemplate: JdbcTemplate,
) : DescribeSpec({
        fun createWord() =
            wordRepository.save(
                WordEntity.fromDomain(
                    WordFactory.createWord(),
                ),
            )

        afterEach {
            jdbcTemplate.cleanUp()
        }

        describe("CreateSentenceApplication") {
            context("when word is not found") {
                it("should raise error") {
                    // given
                    val wordId = 0L

                    // when
                    val exception =
                        shouldThrow<NotFoundException> {
                            createSentenceApplication.run(wordId)
                        }

                    // then
                    exception.message shouldBe "Word not found"
                    exception.code shouldBe "WORD_4"
                }
            }

            context("when word is found") {
                it("should create sentences for word") {
                    // given
                    val word = createWord()
                    word.definitions.size shouldBe 1
                    val wordId = word.id
                    val wordDefinition = word.definitions.first()

                    doReturn(
                        OpenAIWordDefinitionSentenceResponse(
                            sentences =
                                listOf(
                                    Sentence(
                                        content = "I read a book",
                                        translation = "나는 책을 읽었다",
                                        lexicalCategories =
                                            listOf(
                                                Sentence.LexicalCategoryMap(
                                                    word = "I",
                                                    lexicalCategory = "pronoun",
                                                ),
                                                Sentence.LexicalCategoryMap(
                                                    word = "read",
                                                    lexicalCategory = "verb",
                                                ),
                                                Sentence.LexicalCategoryMap(
                                                    word = "a",
                                                    lexicalCategory = "article",
                                                ),
                                                Sentence.LexicalCategoryMap(
                                                    word = "book",
                                                    lexicalCategory = "noun",
                                                ),
                                            ),
                                    ),
                                ),
                        ),
                    ).`when`(openAIService).fetchWordDefinitionSentence(
                        any(),
                        any(),
                        any(),
                        any(),
                    )

                    // when
                    createSentenceApplication.run(wordId)

                    // then
                    val sentences = wordService.getSentencesByWordDefinitionId(wordDefinition.id)
                    sentences.size shouldBe 1
                    sentences.first().content shouldBe "I read a book"
                    sentences.first().translation shouldBe "나는 책을 읽었다"
                    sentences.first().lexicalCategories.size shouldBe 4
                }
            }
        }
    })

package com.inout.apiserver.application.word

import com.inout.apiserver.base.service.openai.OpenAIService
import com.inout.apiserver.base.service.openai.dto.OpenAIWordDefinitionSentenceResponse
import com.inout.apiserver.base.service.openai.dto.Sentence
import com.inout.apiserver.domain.word.WordFactory
import com.inout.apiserver.domain.word.WordService
import com.inout.apiserver.error.NotFoundException
import com.inout.apiserver.extension.cleanUp
import com.inout.apiserver.helper.InOutSpringBootTest
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
    @SpyBean
    private val openAIService: OpenAIService,
    private val wordService: WordService,
    // factories
    private val wordFactory: WordFactory,
    // etc
    private val jdbcTemplate: JdbcTemplate,
) : DescribeSpec({
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
                    val word = wordFactory.createWord()
                    word.definitions.size shouldBe 1
                    val wordId = word.id
                    val wordDefinition = word.definitions.first()
                    val sentences =
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
                            Sentence(
                                content = "I wrote a book",
                                translation = "나는 책을 썼다",
                                lexicalCategories =
                                    listOf(
                                        Sentence.LexicalCategoryMap(
                                            word = "I",
                                            lexicalCategory = "pronoun",
                                        ),
                                        Sentence.LexicalCategoryMap(
                                            word = "wrote",
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
                        )

                    doReturn(OpenAIWordDefinitionSentenceResponse(sentences = sentences))
                        .`when`(openAIService)
                        .fetchWordDefinitionSentence(any(), any(), any(), any())

                    // when
                    createSentenceApplication.run(wordId)

                    // then
                    val result = wordService.getSentencesByWordDefinitionId(wordDefinition.id)
                    result.size shouldBe sentences.size
                    result.first { it.content == "I read a book" }.let { sentence ->
                        sentence.wordDefinitionId shouldBe wordDefinition.id
                        sentence.translation shouldBe "나는 책을 읽었다"
                        sentence.lexicalCategories.size shouldBe 4
                    }
                    result.first { it.content == "I wrote a book" }.let { sentence ->
                        sentence.wordDefinitionId shouldBe wordDefinition.id
                        sentence.translation shouldBe "나는 책을 썼다"
                        sentence.lexicalCategories.size shouldBe 4
                    }
                }
            }
        }
    })

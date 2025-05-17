package com.inout.apiserver.application.word

import com.inout.apiserver.base.service.DictionaryService
import com.inout.apiserver.domain.word.WordFactory
import com.inout.apiserver.error.NotFoundException
import com.inout.apiserver.extension.cleanUp
import com.inout.apiserver.helper.InOutSpringBootTest
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.bson.types.ObjectId
import org.jobrunr.jobs.JobId
import org.jobrunr.scheduling.JobScheduler
import org.mockito.kotlin.any
import org.mockito.kotlin.clearInvocations
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.data.mongodb.core.MongoTemplate
import java.util.UUID

@InOutSpringBootTest
class CreateSentenceApplicationTest(
    private val subject: CreateSentenceApplication,
    // services
    @SpyBean
    private val dictionaryService: DictionaryService,
    // factories
    private val wordFactory: WordFactory,
    // etc
    private val mongoTemplate: MongoTemplate,
    @SpyBean
    private val jobScheduler: JobScheduler,
) : DescribeSpec({
        beforeEach {
            doReturn(JobId(UUID.randomUUID()))
                .whenever(jobScheduler)
                .enqueue(any())
        }

        afterEach {
            mongoTemplate.cleanUp()
            clearInvocations(jobScheduler)
        }

        describe("CreateSentenceApplication") {
            context("when word is not found") {
                it("should raise error") {
                    // given
                    val wordId = ObjectId()

                    // when
                    val exception =
                        shouldThrow<NotFoundException> {
                            subject.run(
                                CreateSentenceApplication.Request(
                                    wordId = wordId,
                                ),
                            )
                        }

                    // then
                    exception.message shouldBe "Word not found"
                    exception.code shouldBe "WORD_4"
                }
            }

            context("when word is found") {
                it("should queue for creating sentences") {
                    // given
                    val word = wordFactory.createWord()
                    val wordId = word.id!!
                    val sentences =
                        listOf(
                            DictionaryService.Sentence(
                                content = "I read a book",
                                translation = "나는 책을 읽었다",
                                lexicalCategories =
                                    listOf(
                                        DictionaryService.LexicalCategory(
                                            word = "I",
                                            lexicalCategory = "pronoun",
                                        ),
                                        DictionaryService.LexicalCategory(
                                            word = "read",
                                            lexicalCategory = "verb",
                                        ),
                                        DictionaryService.LexicalCategory(
                                            word = "a",
                                            lexicalCategory = "article",
                                        ),
                                        DictionaryService.LexicalCategory(
                                            word = "book",
                                            lexicalCategory = "noun",
                                        ),
                                    ),
                            ),
                            DictionaryService.Sentence(
                                content = "I wrote a book",
                                translation = "나는 책을 썼다",
                                lexicalCategories =
                                    listOf(
                                        DictionaryService.LexicalCategory(
                                            word = "I",
                                            lexicalCategory = "pronoun",
                                        ),
                                        DictionaryService.LexicalCategory(
                                            word = "wrote",
                                            lexicalCategory = "verb",
                                        ),
                                        DictionaryService.LexicalCategory(
                                            word = "a",
                                            lexicalCategory = "article",
                                        ),
                                        DictionaryService.LexicalCategory(
                                            word = "book",
                                            lexicalCategory = "noun",
                                        ),
                                    ),
                            ),
                        )

                    doReturn(sentences)
                        .whenever(dictionaryService)
                        .fetchWordDefinitionSentences(any(), any(), any(), any())

                    // when
                    subject.run(
                        CreateSentenceApplication.Request(
                            wordId = wordId,
                        ),
                    )

                    // then
                    verify(jobScheduler, times(word.definitions.size)).enqueue(any())
                }
            }
        }
    })

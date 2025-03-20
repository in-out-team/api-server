package com.inout.apiserver.application.word

import com.inout.apiserver.base.enums.LanguageType
import com.inout.apiserver.base.service.openai.DictionaryService
import com.inout.apiserver.domain.word.WordFactory
import com.inout.apiserver.error.BadRequestException
import com.inout.apiserver.error.ConflictException
import com.inout.apiserver.extension.cleanUp
import com.inout.apiserver.helper.InOutSpringBootTest
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.mockito.kotlin.any
import org.mockito.kotlin.clearInvocations
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.whenever
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.jdbc.core.JdbcTemplate

@InOutSpringBootTest
class CreateWordApplicationTest(
    private val subject: CreateWordApplication,
    // services
    @SpyBean
    private val dictionaryService: DictionaryService,
    // factories
    private val wordFactory: WordFactory,
    // etc
    private val jdbcTemplate: JdbcTemplate,
) : DescribeSpec({
        afterEach {
            jdbcTemplate.cleanUp()
        }

        describe("when given word already exists") {
            it("should throw ConflictException") {
                // Given
                val word = wordFactory.createWord()
                val request =
                    CreateWordApplication.Request(
                        name = word.name,
                        fromLanguage = word.fromLanguage,
                        toLanguage = word.toLanguage,
                    )

                // When
                val exception = shouldThrow<ConflictException> { subject.run(request) }

                // Then
                exception.message shouldBe "Word already exists"
                exception.code shouldBe "WORD_1"
            }
        }

        describe("when given word does not exist") {
            describe("when word definition not found") {
                beforeEach {
                    doReturn(emptyList<DictionaryService.Definition>())
                        .whenever(dictionaryService)
                        .fetchWordDefinitions(any(), any(), any())
                }

                afterEach {
                    clearInvocations(dictionaryService)
                }

                it("should throw BadRequestException") {
                    // Given
                    val request =
                        CreateWordApplication.Request(
                            name = "book",
                            fromLanguage = LanguageType.ENGLISH,
                            toLanguage = LanguageType.KOREAN,
                        )

                    // When
                    val exception = shouldThrow<BadRequestException> { subject.run(request) }

                    // Then
                    exception.message shouldBe "Valid Word Definition not found"
                    exception.code shouldBe "WORD_3"
                }
            }

            describe("when word definition is found but there are no valid definitions") {
                beforeEach {
                    doReturn(
                        listOf(
                            DictionaryService.Definition(
                                type = "noun",
                                definition = "얄리얄리얄랄라",
                                preContext = "얄라리얄라",
                            ),
                        ),
                    ).whenever(dictionaryService)
                        .fetchWordDefinitions(any(), any(), any())

                    doReturn(false)
                        .whenever(dictionaryService)
                        .validateWordDefinition(any(), any(), any(), any())
                }

                afterEach {
                    clearInvocations(dictionaryService)
                }

                it("should throw BadRequestException") {
                    // Given
                    val request =
                        CreateWordApplication.Request(
                            name = "book",
                            fromLanguage = LanguageType.ENGLISH,
                            toLanguage = LanguageType.KOREAN,
                        )

                    // When
                    val exception = shouldThrow<BadRequestException> { subject.run(request) }

                    // Then
                    exception.message shouldBe "Valid Word Definition not found"
                    exception.code shouldBe "WORD_3"
                }
            }

            describe("when word definition is found and there are valid definitions") {
                beforeEach {
                    val definitions =
                        listOf(
                            DictionaryService.Definition(
                                type = "noun",
                                definition = "책",
                                preContext = "정보를 얻거나 즐거움을 얻기 위해 읽는 인쇄물",
                            ),
                            DictionaryService.Definition(
                                type = "verb",
                                definition = "예약하다",
                                preContext = "특정한 날짜나 시간에 무엇을 하기 위해 미리 자리를 확보하다",
                            ),
                        )
                    doReturn(definitions)
                        .whenever(dictionaryService)
                        .fetchWordDefinitions(any(), any(), any())

                    doReturn(true)
                        .whenever(dictionaryService)
                        .validateWordDefinition(any(), any(), any(), any())
                }

                afterEach {
                    clearInvocations(dictionaryService)
                }

                it("should return created word") {
                    // Given
                    val request =
                        CreateWordApplication.Request(
                            name = "book",
                            fromLanguage = LanguageType.ENGLISH,
                            toLanguage = LanguageType.KOREAN,
                        )

                    // When
                    val result = subject.run(request)

                    // Then
                    result.word.id shouldNotBe null
                }
            }
        }
    })

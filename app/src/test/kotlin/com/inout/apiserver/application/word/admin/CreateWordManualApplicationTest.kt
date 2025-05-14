package com.inout.apiserver.application.word.admin

import com.inout.apiserver.base.enums.LanguageType
import com.inout.apiserver.domain.word.MongoWordFactory
import com.inout.apiserver.domain.word.WordWithDefinitions
import com.inout.apiserver.error.ConflictException
import com.inout.apiserver.extension.cleanUp
import com.inout.apiserver.helper.InOutSpringBootTest
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.springframework.data.mongodb.core.MongoTemplate

@InOutSpringBootTest
class CreateWordManualApplicationTest(
    private val subject: CreateWordManualApplication,
    // factories
    private val wordFactory: MongoWordFactory,
    // etc
    private val mongoTemplate: MongoTemplate,
) : DescribeSpec({
        afterEach {
            mongoTemplate.cleanUp()
        }

        describe("when word already exists") {
            var word: WordWithDefinitions? = null

            beforeEach {
                word = wordFactory.createWord()
            }

            it("should throw ConflictException") {
                // given
                val request =
                    CreateWordManualApplication.Request(
                        name = word!!.name,
                        fromLanguage = word!!.fromLanguage,
                        toLanguage = word!!.toLanguage,
                    )

                // when
                val exception =
                    shouldThrow<ConflictException> {
                        subject.run(request)
                    }

                // then
                exception.message shouldBe "Word already exists"
                exception.code shouldBe "WORD_1"
            }
        }

        describe("when word does not exist") {
            it("should create word") {
                // given
                val request =
                    CreateWordManualApplication.Request(
                        name = "apple",
                        fromLanguage = LanguageType.ENGLISH,
                        toLanguage = LanguageType.KOREAN,
                    )

                // when
                val response = subject.run(request)

                // then
                response.word.name shouldBe request.name
                response.word.fromLanguage shouldBe request.fromLanguage
                response.word.toLanguage shouldBe request.toLanguage
                response.word.definitions shouldBe emptyList()
            }
        }
    })

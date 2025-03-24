package com.inout.apiserver.application.study

import com.inout.apiserver.domain.study.StudyFactory
import com.inout.apiserver.domain.user.UserFactory
import com.inout.apiserver.domain.word.WordFactory
import com.inout.apiserver.error.ConflictException
import com.inout.apiserver.error.NotFoundException
import com.inout.apiserver.extension.cleanUp
import com.inout.apiserver.helper.InOutSpringBootTest
import com.inout.apiserver.infrastructure.db.user.User
import com.inout.apiserver.infrastructure.db.word.Word
import com.inout.apiserver.infrastructure.db.word.WordRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.springframework.jdbc.core.JdbcTemplate
import java.util.Optional

@InOutSpringBootTest
class CreateStudyApplicationTest(
    private val subject: CreateStudyApplication,
    // factories
    private val userFactory: UserFactory,
    private val wordFactory: WordFactory,
    private val studyFactory: StudyFactory,
    // repositories
    private val wordRepository: WordRepository,
    // etc
    private val jdbcTemplate: JdbcTemplate,
) : DescribeSpec({
        var user: User? = null

        beforeEach {
            user = userFactory.createUser()
        }

        afterEach {
            jdbcTemplate.cleanUp()
        }

        describe("when word definition does not exists") {
            it("should raise error") {
                // given
                val wordDefinitionId = 1L
                wordRepository.findById(wordDefinitionId) shouldBe Optional.empty()

                // when
                val result =
                    shouldThrow<NotFoundException> {
                        subject.run(
                            CreateStudyApplication.Request(
                                user = user!!,
                                wordDefinitionId = wordDefinitionId,
                            ),
                        )
                    }

                // then
                result.message shouldBe "Word Definition not found"
                result.code shouldBe "WORD_2"
            }
        }

        describe("when word definition exists") {
            var word: Word? = null
            var wordDefinitionId = 0L

            beforeEach {
                word = wordFactory.createWord()
                wordDefinitionId = word!!.definitions.first().id!!
            }

            it("should raise error when user is already studying the word definition") {
                // given
                studyFactory.createStudy(userId = user!!.id!!, wordDefinitionId = wordDefinitionId)

                // when
                val result =
                    shouldThrow<ConflictException> {
                        subject.run(
                            CreateStudyApplication.Request(
                                user = user!!,
                                wordDefinitionId = wordDefinitionId,
                            ),
                        )
                    }

                // then
                result.message shouldBe "Study already exists"
                result.code shouldBe "STUDY_1"
            }

            it("should return StudyWord of the created study") {
                // when
                val result =
                    subject.run(
                        CreateStudyApplication.Request(
                            user = user!!,
                            wordDefinitionId = wordDefinitionId,
                        ),
                    )

                // then
                result.study.userId shouldBe user!!.id!!
                result.study.wordDefinitionId shouldBe wordDefinitionId
                result.word shouldBe word!!
            }
        }
    })

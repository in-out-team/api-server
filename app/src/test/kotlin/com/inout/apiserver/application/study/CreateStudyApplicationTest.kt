package com.inout.apiserver.application.study

import com.inout.apiserver.domain.study.MongoStudyFactory
import com.inout.apiserver.domain.user.MongoUserFactory
import com.inout.apiserver.domain.word.MongoWordFactory
import com.inout.apiserver.domain.word.WordWithDefinitions
import com.inout.apiserver.error.ConflictException
import com.inout.apiserver.error.NotFoundException
import com.inout.apiserver.extension.cleanUp
import com.inout.apiserver.helper.InOutSpringBootTest
import com.inout.apiserver.infrastructure.mongo.user.MongoUser
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.MongoTemplate

@InOutSpringBootTest
class CreateStudyApplicationTest(
    private val subject: CreateStudyApplication,
    // factories
    private val userFactory: MongoUserFactory,
    private val wordFactory: MongoWordFactory,
    private val studyFactory: MongoStudyFactory,
    // etc
    private val mongoTemplate: MongoTemplate,
) : DescribeSpec({
        var user: MongoUser? = null

        beforeEach {
            user = userFactory.createUser()
        }

        afterEach {
            mongoTemplate.cleanUp()
        }

        describe("when word definition does not exists") {
            it("should raise error") {
                // given
                val wordDefinitionId = ObjectId()

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
            var word: WordWithDefinitions? = null
            var wordDefinitionId = ObjectId()

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
                result.studyWord.study.userId shouldBe user!!.id!!
                result.studyWord.study.wordDefinitionId shouldBe wordDefinitionId
                result.studyWord.word shouldBe word!!
            }
        }
    })

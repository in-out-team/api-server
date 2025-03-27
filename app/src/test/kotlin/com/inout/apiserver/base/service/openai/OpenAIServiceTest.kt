package com.inout.apiserver.base.service.openai

import com.inout.apiserver.base.enums.SenderType
import com.inout.apiserver.domain.user.UserFactory
import com.inout.apiserver.domain.word.WordFactory
import com.inout.apiserver.extension.cleanUp
import com.inout.apiserver.helper.InOutSpringBootTest
import com.inout.apiserver.infrastructure.db.user.User
import com.inout.apiserver.infrastructure.db.word.Word
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.ints.shouldBeGreaterThan
import org.springframework.jdbc.core.JdbcTemplate

@InOutSpringBootTest
class OpenAIServiceTest(
    private val openAIService: OpenAIService,
    // factories
    private val userFactory: UserFactory,
    private val wordFactory: WordFactory,
    // etc
    private val jdbcTemplate: JdbcTemplate,
) : DescribeSpec({
        afterEach {
            jdbcTemplate.cleanUp()
        }

        xdescribe("fetchConversation") {
            var user: User? = null
            var word: Word? = null
            var wordDefinitionId = 0L

            beforeEach {
                user = userFactory.createUser()
                word = wordFactory.createWord()
                wordDefinitionId = word!!.definitions.first().id!!
            }

            it("should return system response.") {
                // Given
                val conversation = wordFactory.createConversation(user!!.id!!, wordDefinitionId)

                // When
                val response =
                    openAIService.fetchConversation(
                        fromLanguage = "English",
                        toLanguage = "Korean",
                        wordName = word!!.name,
                        wordMeaning = word!!.definitions.first().meaning,
                        messages =
                            listOf(
                                SenderType.SYSTEM to "Let's talk about ${word!!.name}!",
                                SenderType.USER to "I don't know what ${word!!.name} means",
                            ),
                    )

                // Then
                response.length shouldBeGreaterThan 0
            }
        }
    })

package com.inout.apiserver.application.word

import com.inout.apiserver.base.service.openai.OpenAIService
import com.inout.apiserver.base.service.openai.dto.OpenAIWordDefinitionSentenceResponse
import com.inout.apiserver.base.service.openai.dto.Sentence
import com.inout.apiserver.domain.word.WordFactory
import com.inout.apiserver.domain.word.WordService
import com.inout.apiserver.error.NotFoundException
import com.inout.apiserver.helper.BaseIntegrationTest
import com.inout.apiserver.infrastructure.db.word.WordEntity
import com.inout.apiserver.infrastructure.db.word.WordRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.anyString
import org.mockito.Mockito.doReturn
import org.springframework.boot.test.mock.mockito.SpyBean

class CreateSentenceApplicationTest(
    private val createSentenceApplication: CreateSentenceApplication,
    private val wordRepository: WordRepository,
    @SpyBean
    private val openAIService: OpenAIService,
    private val wordService: WordService,
) : BaseIntegrationTest() {
    private fun createWord() =
        wordRepository.save(
            WordEntity.fromDomain(
                WordFactory.createWord(),
            ),
        )

    @Test
    fun `should raise error if word is not found`() {
        // given
        val wordId = 0L

        // when
        val exception =
            assertThrows<NotFoundException> {
                createSentenceApplication.run(wordId)
            }

        // then
        assertEquals("Word not found", exception.message)
        assertEquals("WORD_4", exception.code)
    }

    @Test
    fun `should create sentences for word`() {
        // given
        val word = createWord()
        assertEquals(1, word.definitions.size)
        val wordId = word.id
        val wordDefinition = word.definitions.first()

        doReturn(
            OpenAIWordDefinitionSentenceResponse(
                sentences =
                    listOf(
                        Sentence(
                            content = "content",
                            translation = "translation",
                        ),
                    ),
            ),
        ).`when`(openAIService).fetchWordDefinitionSentence(
            anyString(),
            anyString(),
            anyString(),
            anyString(),
        )

        // when
        createSentenceApplication.run(wordId)

        // then
        val sentences = wordService.getSentencesByWordDefinitionId(word.definitions.first().id)
        assertEquals(1, sentences.size)
        assertEquals("content", sentences.first().content)
        assertEquals("translation", sentences.first().translation)
    }
}

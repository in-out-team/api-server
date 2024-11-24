package com.inout.apiserver.application.word

import com.inout.apiserver.domain.word.SentenceCreateObject
import com.inout.apiserver.domain.word.WordFactory
import com.inout.apiserver.domain.word.WordService
import com.inout.apiserver.error.NotFoundException
import com.inout.apiserver.helper.InOutSpringBootTest
import com.inout.apiserver.infrastructure.db.word.SentenceEntity
import com.inout.apiserver.infrastructure.db.word.SentenceRepository
import com.inout.apiserver.infrastructure.db.word.WordEntity
import com.inout.apiserver.infrastructure.db.word.WordRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@InOutSpringBootTest
class ReadSentencesApplicationTest(
    // services
    private val wordService: WordService,
    // repositories
    private val sentenceRepository: SentenceRepository,
    private val wordRepository: WordRepository,
) {
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

    @Test
    fun `should raise error when sentences not found`() {
        // given
        val wordDefinitionId = 1L

        // when
        val exception =
            assertThrows<NotFoundException> {
                ReadSentencesApplication(wordService).run(wordDefinitionId)
            }

        // then
        assertEquals("Sentences not found for word definition id: $wordDefinitionId", exception.message)
        assertEquals("WORD_3", exception.code)
    }

    @Test
    fun `should return sentences`() {
        // given
        val word = createWord()
        val wordDefinitionId = word.definitions.first().id
        val sentence = createSentence(wordDefinitionId)

        // when
        val result = ReadSentencesApplication(wordService).run(wordDefinitionId)

        // then
        assertEquals(1, result.size)
        val resultSentence = result.first()
        assertEquals(sentence.id, resultSentence.id)
        assertEquals(wordDefinitionId, resultSentence.wordDefinitionId)
        assertEquals("sentence content", resultSentence.content)
    }
}

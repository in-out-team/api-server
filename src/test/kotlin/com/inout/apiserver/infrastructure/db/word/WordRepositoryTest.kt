package com.inout.apiserver.infrastructure.db.word

import com.inout.apiserver.domain.word.Word
import com.inout.apiserver.infrastructure.db.DbTestSupport
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.context.annotation.Import
import org.springframework.dao.DataIntegrityViolationException

@Import(WordRepository::class)
class WordRepositoryTest(
    private val wordJpaRepository: WordJpaRepository,
    private val wordRepository: WordRepository,
) : DbTestSupport() {
    @Test
    fun `save - should raise error when same combination of name and language exists`() {
        // given
        val wordEntity = WordEntity(name = "test", language = LanguageTypes.ENGLISH)
        wordJpaRepository.save(wordEntity)

        // when & then
        assertThatThrownBy {
            wordRepository.save(WordEntity(name = "test", language = LanguageTypes.ENGLISH))
        }
            .isInstanceOf(DataIntegrityViolationException::class.java)
            .hasMessageContaining("could not execute statement")
    }

    @Test
    fun `save - should return new saved word`() {
        // given
        val wordEntity = WordEntity(name = "test", language = LanguageTypes.ENGLISH)

        // when
        val result = wordRepository.save(wordEntity)

        // then
        assertTrue(result is Word)
        assertEquals(wordEntity.name, result.name)
        assertEquals(wordEntity.language, result.language)
        assertNotNull(result.id)
        assertNotNull(result.createdAt)
        assertNotNull(result.updatedAt)
    }

    @Test
    fun `findByNameAndLanguage - should return null when word not found`() {
        // given
        val wordEntity = WordEntity(name = "test", language = LanguageTypes.ENGLISH)
        wordJpaRepository.save(wordEntity)

        // when
        val result = wordRepository.findByNameAndLanguage("1-test", LanguageTypes.ENGLISH)

        // then
        assertNull(result)
    }

    @Test
    fun `findByNameAndLanguage - should return word when word found`() {
        // given
        val wordEntity = WordEntity(name = "test", language = LanguageTypes.ENGLISH)
        wordJpaRepository.save(wordEntity)

        // when
        val result = wordRepository.findByNameAndLanguage("test", LanguageTypes.ENGLISH)

        // then
        assertNotNull(result)
        assertEquals(wordEntity.name, result?.name)
        assertEquals(wordEntity.language, result?.language)
    }

    @Test
    fun `findById - should return null when word not found`() {
        // given
        assertEquals(wordJpaRepository.count(), 0)

        // when
        val result = wordRepository.findById(1L)

        // then
        assertNull(result)
    }

    @Test
    fun `findById - should return word when word found`() {
        // given
        val wordEntity = WordEntity(name = "test", language = LanguageTypes.ENGLISH)
        val savedWord = wordJpaRepository.save(wordEntity)

        // when
        val result = wordRepository.findById(requireNotNull(savedWord.id))

        // then
        assertNotNull(result)
        assertEquals(wordEntity.name, result?.name)
        assertEquals(wordEntity.language, result?.language)
        assertEquals(savedWord.id, result?.id)
        assertEquals(savedWord.createdAt, result?.createdAt)
        assertEquals(savedWord.updatedAt, result?.updatedAt)
    }
}
package com.inout.apiserver.infrastructure.db.word

import com.inout.apiserver.base.enums.LanguageType
import com.inout.apiserver.base.enums.LexicalCategoryType
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
        val wordEntity = createWordEntity()
        wordJpaRepository.save(wordEntity)

        // when & then
        assertThatThrownBy {
            wordRepository.save(
                WordEntity(
                    name = "test",
                    fromLanguage = LanguageType.ENGLISH,
                    toLanguage = LanguageType.KOREAN,
                    definitions = emptyList()
                )
            )
        }
            .isInstanceOf(DataIntegrityViolationException::class.java)
            .hasMessageContaining("could not execute statement")
    }

    @Test
    fun `save - should return new saved word`() {
        // given
        val wordEntity = createWordEntity()

        // when
        val result = wordRepository.save(wordEntity)

        // then
        assertTrue(result is Word)
        assertEquals(wordEntity.name, result.name)
        assertEquals(wordEntity.fromLanguage, result.fromLanguage)
        assertEquals(wordEntity.toLanguage, result.toLanguage)
        assertEquals(wordEntity.definitions.size, result.definitions.size)
        assertEquals(wordEntity.definitions[0].lexicalCategory, result.definitions[0].lexicalCategory)
        assertEquals(wordEntity.definitions[0].meaning, result.definitions[0].meaning)
        assertEquals(wordEntity.definitions[0].preContext, result.definitions[0].preContext)
        assertNotNull(result.id)
        assertNotNull(result.createdAt)
        assertNotNull(result.updatedAt)
        assertNotNull(result.definitions[0].id)
    }

    @Test
    fun `findByNameAndLanguage - should return null when word not found`() {
        // given
        val name = "test"
        val wordEntity = createWordEntity(name = name)
        wordJpaRepository.save(wordEntity)

        // when
        val result = wordRepository.findByNameAndFromLanguageAndToLanguage(
            name = "1-$name",
            fromLanguage = wordEntity.fromLanguage,
            toLanguage = wordEntity.toLanguage
        )

        // then
        assertNull(result)
    }

    @Test
    fun `findByNameAndLanguage - should return word when word found`() {
        // given
        val name = "test"
        val wordEntity = createWordEntity(name = name)
        wordJpaRepository.save(wordEntity)

        // when
        val result = wordRepository.findByNameAndFromLanguageAndToLanguage(
            name = name,
            fromLanguage = wordEntity.fromLanguage,
            toLanguage = wordEntity.toLanguage
        )

        // then
        assertNotNull(result)
        assertEquals(wordEntity.name, result?.name)
        assertEquals(wordEntity.fromLanguage, result?.fromLanguage)
        assertEquals(wordEntity.toLanguage, result?.toLanguage)
        assertEquals(wordEntity.definitions.size, result?.definitions?.size)
        assertEquals(wordEntity.definitions[0].lexicalCategory, result?.definitions?.get(0)?.lexicalCategory)
        assertEquals(wordEntity.definitions[0].meaning, result?.definitions?.get(0)?.meaning)
        assertEquals(wordEntity.definitions[0].preContext, result?.definitions?.get(0)?.preContext)
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
        val wordEntity = createWordEntity()
        val savedWord = wordJpaRepository.save(wordEntity)

        // when
        val result = wordRepository.findById(requireNotNull(savedWord.id))

        // then
        assertNotNull(result)
        assertEquals(wordEntity.name, result?.name)
        assertEquals(wordEntity.fromLanguage, result?.fromLanguage)
        assertEquals(wordEntity.toLanguage, result?.toLanguage)
        assertEquals(wordEntity.definitions.size, result?.definitions?.size)
        assertEquals(wordEntity.definitions[0].lexicalCategory, result?.definitions?.get(0)?.lexicalCategory)
        assertEquals(wordEntity.definitions[0].meaning, result?.definitions?.get(0)?.meaning)
        assertEquals(wordEntity.definitions[0].preContext, result?.definitions?.get(0)?.preContext)
        assertEquals(savedWord.id, result?.id)
        assertEquals(savedWord.createdAt, result?.createdAt)
        assertEquals(savedWord.updatedAt, result?.updatedAt)
        assertEquals(savedWord.definitions[0].id, result?.definitions?.get(0)?.id)
    }

    private fun createWordEntity(
        name: String = "test",
        fromLanguage: LanguageType = LanguageType.ENGLISH,
        toLanguage: LanguageType = LanguageType.KOREAN
    ): WordEntity {
        return WordEntity(
            name = name, fromLanguage = fromLanguage, toLanguage = toLanguage, definitions = listOf(
                WordDefinitionEntity(
                    lexicalCategory = LexicalCategoryType.NOUN,
                    meaning = "test",
                    preContext = "test preContext"
                ),
            )
        )
    }
}
package com.inout.apiserver.infrastructure.db.word

import com.inout.apiserver.base.enums.LanguageType
import com.inout.apiserver.base.enums.LexicalCategoryType
import com.inout.apiserver.domain.word.Word
import com.inout.apiserver.domain.word.WordCreateObject
import com.inout.apiserver.domain.word.WordDefinition
import com.inout.apiserver.domain.word.WordDefinitionCreateObject
import com.inout.apiserver.error.InOutRequireNotNullException
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime

class WordEntityTest {
    private val now = LocalDateTime.now()

    @Test
    fun `toDomain - raises error when id is null`() {
        // given
        val wordDefinitionEntity = createWordDefinitionEntity()
        val wordEntity = WordEntity(
            name = "name",
            fromLanguage = LanguageType.ENGLISH,
            toLanguage = LanguageType.KOREAN,
            definitions = listOf(wordDefinitionEntity)
        )

        // when & then
        val error = assertThrows<InOutRequireNotNullException> {
            wordEntity.toDomain()
        }
        assertEquals("Word id is null", error.message)
        assertEquals("IORNN_WORD_1", error.code)
    }

    @Test
    fun `toDomain - returns Word`() {
        // given
        val name = "name"
        val fromLanguage = LanguageType.ENGLISH
        val toLanguage = LanguageType.KOREAN
        val wordDefinitionEntity = createWordDefinitionEntity().apply { id = 1L }
        val wordEntity = WordEntity(
            name = name,
            fromLanguage = fromLanguage,
            toLanguage = toLanguage,
            definitions = listOf(wordDefinitionEntity)
        )
            .apply {
                id = 1L
                createdAt = now
                updatedAt = now
            }

        // when
        val word = wordEntity.toDomain()

        // then
        assertTrue(word is Word)
        assertEquals(1L, word.id)
        assertEquals(name, word.name)
        assertEquals(fromLanguage, word.fromLanguage)
        assertEquals(toLanguage, word.toLanguage)
        assertEquals(now, word.createdAt)
        assertEquals(now, word.updatedAt)
    }

    @Test
    fun `of - returns WordEntity`() {
        // given
        val name = "name"
        val fromLanguage = LanguageType.ENGLISH
        val toLanguage = LanguageType.KOREAN
        val wordDefinition = createWordDefinition()
        val word = Word(
            id = 1L,
            name = name,
            fromLanguage = fromLanguage,
            toLanguage = toLanguage,
            definitions = listOf(wordDefinition),
            createdAt = now,
            updatedAt = now
        )

        // when
        val wordEntity = WordEntity.of(word)

        // then
        assertTrue(wordEntity is WordEntity)
        assertEquals(1L, wordEntity.id)
        assertEquals(name, wordEntity.name)
        assertEquals(fromLanguage, wordEntity.fromLanguage)
        assertEquals(toLanguage, wordEntity.toLanguage)
        assertEquals(now, wordEntity.createdAt)
        assertEquals(now, wordEntity.updatedAt)
    }

    @Test
    fun `fromCreateObject - returns WordEntity`() {
        // given
        val name = "name"
        val fromLanguage = LanguageType.ENGLISH
        val toLanguage = LanguageType.KOREAN

        // when
        val wordEntity = WordEntity.fromCreateObject(createWordCreateObject())

        // then
        assertTrue(wordEntity is WordEntity)
        assertNull(wordEntity.id)
        assertEquals(name, wordEntity.name)
        assertEquals(fromLanguage, wordEntity.fromLanguage)
        assertEquals(toLanguage, wordEntity.toLanguage)
        assertNull(wordEntity.createdAt)
        assertNull(wordEntity.updatedAt)
    }

    private fun createWordDefinitionEntity(): WordDefinitionEntity {
        return WordDefinitionEntity(
            lexicalCategory = LexicalCategoryType.NOUN,
            meaning = "meaning",
            preContext = "preContext"
        )
    }

    private fun createWordDefinition(): WordDefinition {
        return WordDefinition(
            id = 1L,
            lexicalCategory = LexicalCategoryType.NOUN,
            meaning = "meaning",
            preContext = "preContext"
        )
    }

    private fun createWordDefinitionCreateObject(): WordDefinitionCreateObject {
        return WordDefinitionCreateObject(
            lexicalCategory = LexicalCategoryType.NOUN,
            meaning = "meaning",
            preContext = "preContext"
        )
    }

    private fun createWordCreateObject(): WordCreateObject {
        return WordCreateObject(
            name = "name",
            fromLanguage = LanguageType.ENGLISH,
            toLanguage = LanguageType.KOREAN,
            definitions = listOf(createWordDefinitionCreateObject())
        )
    }
}
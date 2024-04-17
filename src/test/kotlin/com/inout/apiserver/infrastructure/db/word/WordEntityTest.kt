package com.inout.apiserver.infrastructure.db.word

import com.inout.apiserver.domain.word.Word
import com.inout.apiserver.domain.word.WordCreateObject
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
        val wordEntity = WordEntity(
            name = "name",
            language = LanguageTypes.ENGLISH
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
        val language = LanguageTypes.ENGLISH
        val wordEntity = WordEntity(name = name, language = language)
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
        assertEquals(language, word.language)
        assertEquals(now, word.createdAt)
        assertEquals(now, word.updatedAt)
    }

    @Test
    fun `of - returns WordEntity`() {
        // given
        val name = "name"
        val language = LanguageTypes.ENGLISH
        val word = Word(
            id = 1L,
            name = name,
            language = language,
            createdAt = now,
            updatedAt = now
        )

        // when
        val wordEntity = WordEntity.of(word)

        // then
        assertTrue(wordEntity is WordEntity)
        assertEquals(1L, wordEntity.id)
        assertEquals(name, wordEntity.name)
        assertEquals(language, wordEntity.language)
        assertEquals(now, wordEntity.createdAt)
        assertEquals(now, wordEntity.updatedAt)
    }

    @Test
    fun `fromCreateObject - returns WordEntity`() {
        // given
        val name = "name"
        val language = LanguageTypes.ENGLISH

        // when
        val wordEntity = WordEntity.fromCreateObject(WordCreateObject(name = name, language = language))

        // then
        assertTrue(wordEntity is WordEntity)
        assertNull(wordEntity.id)
        assertEquals(name, wordEntity.name)
        assertEquals(language, wordEntity.language)
        assertNull(wordEntity.createdAt)
        assertNull(wordEntity.updatedAt)
    }
}
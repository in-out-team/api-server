package com.inout.apiserver.application.word

import com.inout.apiserver.base.enums.LanguageType
import com.inout.apiserver.base.enums.LexicalCategoryType
import com.inout.apiserver.domain.word.WordService
import com.inout.apiserver.infrastructure.db.word.Word
import com.inout.apiserver.infrastructure.db.word.WordDefinition
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest

class ReadWordsApplicationTest {
    private val wordService = mockk<WordService>()
    private val readWordsApplication = ReadWordsApplication(wordService)

    private fun wordsList() =
        listOf(
            Word(
                id = 1L,
                name = "book",
                fromLanguage = LanguageType.ENGLISH,
                toLanguage = LanguageType.KOREAN,
                definitions =
                    listOf(
                        WordDefinition(
                            id = 1L,
                            lexicalCategory = LexicalCategoryType.NOUN,
                            meaning = "책",
                            preContext = "정보를 얻거나 즐거움을 얻기 위해 읽는 인쇄물",
                        ),
                        WordDefinition(
                            id = 2L,
                            lexicalCategory = LexicalCategoryType.VERB,
                            meaning = "예약하다",
                            preContext = "특정한 날짜나 시간에 무엇을 하기 위해 미리 자리를 확보하다",
                        ),
                    ),
            ),
            Word(
                id = 2L,
                name = "booked",
                fromLanguage = LanguageType.ENGLISH,
                toLanguage = LanguageType.KOREAN,
                definitions =
                    listOf(
                        WordDefinition(
                            id = 3L,
                            lexicalCategory = LexicalCategoryType.ADJECTIVE,
                            meaning = "예약된",
                            preContext = "미리 자리를 확보한",
                        ),
                        WordDefinition(
                            id = 4L,
                            lexicalCategory = LexicalCategoryType.VERB,
                            meaning = "예약하다",
                            preContext = "특정한 날짜나 시간에 무엇을 하기 위해 미리 자리를 확보하다",
                        ),
                    ),
            ),
        )

    @Test
    fun `run - should return words`() {
        // given
        val fromLanguage = LanguageType.ENGLISH
        val toLanguage = LanguageType.KOREAN
        val prefix = "book"
        val lexicalCategoryType = null
        val pageable = PageRequest.of(0, 1)
        val words = wordsList()
        every {
            wordService.getWordsWithDefinitions(fromLanguage, toLanguage, prefix, lexicalCategoryType, pageable)
        } returns PageImpl(listOf(words.first()), pageable, words.size.toLong())

        // when
        val (totalCount, content) =
            readWordsApplication.run(
                ReadWordsApplication.Request(
                    fromLanguage = fromLanguage,
                    toLanguage = toLanguage,
                    prefix = prefix,
                    lexicalCategoryType = lexicalCategoryType,
                    pageable = pageable,
                ),
            )

        // then
        assertEquals(2, totalCount)
        assertEquals(1, content.size)
        assertEquals(words.first(), content.first())
    }
}

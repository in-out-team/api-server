package com.inout.apiserver.domain.word

import com.inout.apiserver.base.enums.LanguageType
import com.inout.apiserver.base.enums.LexicalCategoryType
import java.time.Instant

class WordFactory {
    companion object {
        fun createWord(
            id: Long = 1L,
            name: String = "book",
            fromLanguage: LanguageType = LanguageType.ENGLISH,
            toLanguage: LanguageType = LanguageType.KOREAN,
            lexicalCategory: LexicalCategoryType = LexicalCategoryType.NOUN,
            meaning: String = "책",
            preContext: String = "정보를 얻거나 즐거움을 얻기 위해 읽는 인쇄물",
        ): Word {
            val now = Instant.now()
            val wordDefinition =
                WordDefinition(
                    id = id,
                    lexicalCategory = lexicalCategory,
                    meaning = meaning,
                    preContext = preContext,
                )
            return Word(
                id = id,
                name = name,
                fromLanguage = fromLanguage,
                toLanguage = toLanguage,
                definitions = listOf(wordDefinition),
                createdAt = now,
                updatedAt = now,
            )
        }
    }
}

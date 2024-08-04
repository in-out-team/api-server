package com.inout.apiserver.application.word

import com.inout.apiserver.base.enums.LanguageType
import com.inout.apiserver.base.enums.LexicalCategoryType
import com.inout.apiserver.domain.word.Word
import com.inout.apiserver.infrastructure.db.word.WordRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component

@Component
class ReadWordsApplication(
    private val wordRepository: WordRepository,
) {
    fun run(
        fromLanguage: LanguageType,
        toLanguage: LanguageType,
        prefix: String,
        lexicalCategoryType: LexicalCategoryType?,
        pageable: Pageable
    ): Pair<Long, List<Word>> {
        val words =
            wordRepository.findWordsWithDefinitions(fromLanguage, toLanguage, prefix, lexicalCategoryType, pageable)

        return Pair(words.totalElements, words.content)
    }
}

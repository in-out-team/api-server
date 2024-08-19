package com.inout.apiserver.application.word

import com.inout.apiserver.base.enums.LanguageType
import com.inout.apiserver.base.enums.LexicalCategoryType
import com.inout.apiserver.domain.word.Word
import com.inout.apiserver.domain.word.WordService
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component

@Component
class ReadWordsApplication(
    private val wordService: WordService,
) {
    fun run(
        fromLanguage: LanguageType,
        toLanguage: LanguageType,
        prefix: String,
        lexicalCategoryType: LexicalCategoryType?,
        pageable: Pageable,
    ): Pair<Long, List<Word>> {
        val words =
            wordService.getWordsWithDefinitions(fromLanguage, toLanguage, prefix, lexicalCategoryType, pageable)

        return Pair(words.totalElements, words.content)
    }
}

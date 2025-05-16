package com.inout.apiserver.application.word

import com.inout.apiserver.base.enums.LanguageType
import com.inout.apiserver.base.enums.LexicalCategoryType
import com.inout.apiserver.domain.word.WordService
import com.inout.apiserver.domain.word.WordWithDefinitions
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component

@Component
class AdminReadWordsApplication(
    private val wordService: WordService,
) {
    data class Request(
        val fromLanguage: LanguageType,
        val toLanguage: LanguageType,
        val prefix: String,
        val lexicalCategoryType: LexicalCategoryType?,
        val pageable: Pageable,
    )

    data class Response(
        val totalCount: Long,
        val words: List<WordWithDefinitions>,
    )

    fun run(request: Request): Response {
        val wordsPage =
            wordService.getWordsWithDefinitions(
                fromLanguage = request.fromLanguage,
                toLanguage = request.toLanguage,
                prefix = request.prefix,
                lexicalCategory = request.lexicalCategoryType,
                pageable = request.pageable,
            )

        return Response(
            totalCount = wordsPage.totalElements,
            words = wordsPage.content,
        )
    }
}

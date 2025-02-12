package com.inout.apiserver.application.word

import com.inout.apiserver.base.enums.LanguageType
import com.inout.apiserver.base.enums.LexicalCategoryType
import com.inout.apiserver.domain.word.WordService
import com.inout.apiserver.infrastructure.db.word.Word
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component

@Component
class ReadWordsApplication(
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
        val words: List<Word>,
    )

    fun run(request: Request): Response {
        val words =
            wordService.getWordsWithDefinitions(
                fromLanguage = request.fromLanguage,
                toLanguage = request.toLanguage,
                prefix = request.prefix,
                lexicalCategory = request.lexicalCategoryType,
                pageable = request.pageable,
            )

        return Response(
            totalCount = words.totalElements,
            words = words.content,
        )
    }
}

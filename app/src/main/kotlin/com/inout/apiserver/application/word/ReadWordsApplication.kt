package com.inout.apiserver.application.word

import com.inout.apiserver.base.enums.LanguageType
import com.inout.apiserver.base.enums.LexicalCategoryType
import com.inout.apiserver.domain.study.MongoStudyService
import com.inout.apiserver.domain.word.MongoWordService
import com.inout.apiserver.domain.word.WordWithDefinitions
import com.inout.apiserver.infrastructure.mongo.user.MongoUser
import org.bson.types.ObjectId
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component

@Component
class ReadWordsApplication(
    private val wordService: MongoWordService,
    private val studyService: MongoStudyService,
) {
    data class Request(
        val fromLanguage: LanguageType,
        val toLanguage: LanguageType,
        val prefix: String,
        val lexicalCategoryType: LexicalCategoryType?,
        val pageable: Pageable,
        val user: MongoUser,
    )

    data class Response(
        val totalCount: Long,
        val words: List<WordWithDefinitions>,
        val studyingWordDefinitionIds: List<ObjectId>,
    )

    fun run(request: Request): Response {
        val wordsPage =
            wordService.getWordsWithLiveDefinitions(
                fromLanguage = request.fromLanguage,
                toLanguage = request.toLanguage,
                prefix = request.prefix,
                lexicalCategory = request.lexicalCategoryType,
                pageable = request.pageable,
            )
        val studyingWordDefinitionIds =
            studyService
                .getStudiesByUserIdAndWordDefinitionIds(
                    userId = request.user.id!!,
                    wordDefinitionIds =
                        wordsPage.content
                            .flatMap { it.definitions }
                            .map { it.id!! },
                ).map { it.wordDefinitionId }

        return Response(
            totalCount = wordsPage.totalElements,
            words = wordsPage.content,
            studyingWordDefinitionIds = studyingWordDefinitionIds,
        )
    }
}

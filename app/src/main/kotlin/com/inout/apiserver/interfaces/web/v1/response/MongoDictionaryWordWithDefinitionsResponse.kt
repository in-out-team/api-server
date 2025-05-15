package com.inout.apiserver.interfaces.web.v1.response

import com.inout.apiserver.base.enums.LanguageType
import com.inout.apiserver.base.enums.LexicalCategoryType
import com.inout.apiserver.domain.word.WordWithDefinitions
import org.bson.types.ObjectId

data class MongoDictionaryWordWithDefinitionsResponse(
    val id: ObjectId,
    val name: String,
    val fromLanguage: LanguageType,
    val toLanguage: LanguageType,
    val definitions: List<MongoWordDefinitionWithStudyingResponse>,
) {
    companion object {
        fun of(
            word: WordWithDefinitions,
            lexicalCategory: LexicalCategoryType?,
            studyingWordDefinitionIds: Set<ObjectId>,
        ): MongoDictionaryWordWithDefinitionsResponse =
            MongoDictionaryWordWithDefinitionsResponse(
                id = word.id!!,
                name = word.name,
                fromLanguage = word.fromLanguage,
                toLanguage = word.toLanguage,
                definitions =
                    word.definitions
                        .filter {
                            lexicalCategory == null || it.lexicalCategory == lexicalCategory
                        }.map {
                            MongoWordDefinitionWithStudyingResponse.of(it, it.id in studyingWordDefinitionIds)
                        },
            )
    }
}

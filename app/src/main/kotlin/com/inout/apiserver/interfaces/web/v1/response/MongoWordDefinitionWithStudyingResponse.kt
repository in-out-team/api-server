package com.inout.apiserver.interfaces.web.v1.response

import com.inout.apiserver.base.enums.LexicalCategoryType
import com.inout.apiserver.domain.word.WordWithDefinitions
import org.bson.types.ObjectId

data class MongoWordDefinitionWithStudyingResponse(
    val id: ObjectId,
    val lexicalCategory: LexicalCategoryType,
    val meaning: String,
    val preContext: String,
    val studying: Boolean,
) {
    companion object {
        fun of(
            wordDefinition: WordWithDefinitions.WordDefinition,
            studying: Boolean,
        ): MongoWordDefinitionWithStudyingResponse =
            MongoWordDefinitionWithStudyingResponse(
                id = wordDefinition.id!!,
                lexicalCategory = wordDefinition.lexicalCategory,
                meaning = wordDefinition.meaning,
                preContext = wordDefinition.preContext,
                studying = studying,
            )
    }
}

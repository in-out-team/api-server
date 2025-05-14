package com.inout.apiserver.interfaces.web.v1.response

import com.inout.apiserver.base.enums.LexicalCategoryType
import com.inout.apiserver.base.enums.StatusType
import com.inout.apiserver.domain.word.WordWithDefinitions
import org.bson.types.ObjectId

data class MongoWordDefinitionResponse(
    val id: ObjectId,
    val lexicalCategory: LexicalCategoryType,
    val meaning: String,
    val preContext: String,
    val status: StatusType,
) {
    companion object {
        fun of(wordDefinition: WordWithDefinitions.WordDefinition): MongoWordDefinitionResponse =
            MongoWordDefinitionResponse(
                id = wordDefinition.id!!,
                lexicalCategory = wordDefinition.lexicalCategory,
                meaning = wordDefinition.meaning,
                preContext = wordDefinition.preContext,
                status = wordDefinition.status,
            )
    }
}

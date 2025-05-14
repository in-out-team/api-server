package com.inout.apiserver.domain.word

import com.inout.apiserver.base.enums.LanguageType
import com.inout.apiserver.base.enums.LexicalCategoryType
import com.inout.apiserver.base.enums.StatusType
import com.inout.apiserver.infrastructure.mongo.word.MongoWord
import com.inout.apiserver.infrastructure.mongo.word.MongoWordDefinition
import org.bson.types.ObjectId

data class WordWithDefinitions(
    val id: ObjectId? = null,
    val name: String,
    val fromLanguage: LanguageType,
    val toLanguage: LanguageType,
    val definitions: List<WordDefinition> = emptyList(),
) {
    data class WordDefinition(
        val id: ObjectId? = null,
        val lexicalCategory: LexicalCategoryType,
        val meaning: String,
        val preContext: String,
        val status: StatusType,
    )

    companion object {
        fun of(
            word: MongoWord,
            definitions: List<MongoWordDefinition>,
        ): WordWithDefinitions =
            WordWithDefinitions(
                id = word.id,
                name = word.name,
                fromLanguage = word.fromLanguage,
                toLanguage = word.toLanguage,
                definitions =
                    definitions.map {
                        WordDefinition(
                            id = it.id,
                            lexicalCategory = it.lexicalCategory,
                            meaning = it.meaning,
                            preContext = it.preContext,
                            status = it.status,
                        )
                    },
            )
    }
}

package com.inout.apiserver.infrastructure.mongo.word

import com.inout.apiserver.base.enums.LexicalCategoryType
import com.inout.apiserver.base.enums.SentenceType
import org.bson.types.ObjectId
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.index.CompoundIndexes
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document(collection = "sentences")
@CompoundIndexes(
    CompoundIndex(
        name = "idx_wordDefinitionId",
        def = "{'wordDefinitionId': 1}",
    ),
)
data class MongoSentence(
    @Id
    val id: ObjectId? = null,
    val wordDefinitionId: ObjectId,
    val type: SentenceType,
    val content: String,
    val translation: String,
    val lexicalCategories: List<LexicalCategoryInfo>,
    @CreatedDate
    val createdAt: Instant? = null,
    @LastModifiedDate
    val updatedAt: Instant? = null,
) {
    data class LexicalCategoryInfo(
        val word: String,
        val lexicalCategory: LexicalCategoryType,
    )

    companion object {
        // FIXME: temporary
        fun fromCreateObject(
            wordDefinitionId: ObjectId,
            type: SentenceType,
            content: String,
            translation: String,
            lexicalCategories: List<LexicalCategoryInfo>,
        ): MongoSentence =
            MongoSentence(
                wordDefinitionId = wordDefinitionId,
                type = type,
                content = content,
                translation = translation,
                lexicalCategories = lexicalCategories,
            )
    }
}

package com.inout.apiserver.infrastructure.mongo.word

import com.inout.apiserver.base.enums.LexicalCategoryType
import com.inout.apiserver.base.enums.StatusType
import com.inout.apiserver.domain.word.WordDefinitionCreateObject
import org.bson.types.ObjectId
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document(collection = "word_definitions")
data class MongoWordDefinition(
    @Id
    val id: ObjectId? = null,
    val wordId: ObjectId,
    val lexicalCategory: LexicalCategoryType,
    val meaning: String,
    val preContext: String,
    val status: StatusType = StatusType.PENDING,
    @CreatedDate
    val createdAt: Instant? = null,
    @LastModifiedDate
    val updatedAt: Instant? = null,
) {
    companion object {
        fun fromCreateObject(
            createObject: WordDefinitionCreateObject,
            wordId: ObjectId,
        ): MongoWordDefinition =
            MongoWordDefinition(
                wordId = wordId,
                lexicalCategory = createObject.lexicalCategory,
                meaning = createObject.meaning,
                preContext = createObject.preContext,
                status = StatusType.PENDING,
            )
    }
}

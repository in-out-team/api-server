package com.inout.apiserver.infrastructure.mongo.word

import com.inout.apiserver.base.enums.LanguageType
import com.inout.apiserver.domain.word.WordCreateObject
import org.bson.types.ObjectId
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document(collection = "words")
data class MongoWord(
    @Id
    val id: ObjectId? = null,
    val name: String,
    val fromLanguage: LanguageType,
    val toLanguage: LanguageType,
    @CreatedDate
    val createdAt: Instant? = null,
    @LastModifiedDate
    val updatedAt: Instant? = null,
) {
    companion object {
        fun fromCreateObject(createObject: WordCreateObject): MongoWord =
            MongoWord(
                name = createObject.name,
                fromLanguage = createObject.fromLanguage,
                toLanguage = createObject.toLanguage,
            )
    }
}

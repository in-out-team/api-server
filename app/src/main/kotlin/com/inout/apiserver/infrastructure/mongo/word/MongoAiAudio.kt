package com.inout.apiserver.infrastructure.mongo.word

import com.inout.apiserver.base.enums.AiVoiceType
import com.inout.apiserver.base.enums.LanguageType
import com.inout.apiserver.domain.word.AiAudioCreateObject
import org.bson.types.ObjectId
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.index.CompoundIndexes
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document(collection = "ai_audios")
@CompoundIndexes(
    CompoundIndex(
        name = "unique_language_voiceType_content",
        def = "{'language': 1, 'voiceType': 1, 'content': 1}",
        unique = true,
    ),
)
data class MongoAiAudio(
    @Id
    val id: ObjectId? = null,
    val language: LanguageType,
    val voiceType: AiVoiceType,
    val content: String,
    val directory: String,
    @CreatedDate
    val createdAt: Instant? = null,
    @LastModifiedDate
    val updatedAt: Instant? = null,
) {
    companion object {
        fun fromCreateObject(createObject: AiAudioCreateObject): MongoAiAudio =
            MongoAiAudio(
                language = createObject.language,
                voiceType = createObject.aiVoiceType,
                content = createObject.content,
                directory = createObject.directory,
            )
    }
}

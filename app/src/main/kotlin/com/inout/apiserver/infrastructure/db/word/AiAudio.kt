package com.inout.apiserver.infrastructure.db.word

import com.inout.apiserver.base.alias.AiAudioId
import com.inout.apiserver.base.enums.AiVoiceType
import com.inout.apiserver.base.enums.LanguageType
import com.inout.apiserver.domain.word.AiAudioCreateObject
import com.inout.apiserver.infrastructure.db.TimestampedEntity
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table

@Entity
@Table(
    name = "ai_audios",
    indexes = [
        Index(
            name = "idx_ai_audios_language_voice_type_content",
            columnList = "language, voice_type, content",
        ),
    ],
)
data class AiAudio(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: AiAudioId? = null,
    @Enumerated(EnumType.STRING)
    val language: LanguageType,
    @Enumerated(EnumType.STRING)
    val voiceType: AiVoiceType,
    val content: String,
    val directory: String,
) : TimestampedEntity() {
    companion object {
        fun fromCreateObject(createObject: AiAudioCreateObject): AiAudio =
            AiAudio(
                language = createObject.language,
                voiceType = createObject.aiVoiceType,
                content = createObject.content,
                directory = createObject.directory,
            )
    }
}

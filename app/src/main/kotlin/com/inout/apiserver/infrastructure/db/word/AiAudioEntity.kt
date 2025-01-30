package com.inout.apiserver.infrastructure.db.word

import com.inout.apiserver.base.enums.AiVoiceType
import com.inout.apiserver.base.enums.LanguageType
import com.inout.apiserver.domain.word.AiAudio
import com.inout.apiserver.error.InOutRequireNotNullException
import com.inout.apiserver.infrastructure.db.BaseEntity
import jakarta.persistence.Entity
import jakarta.persistence.Table

@Entity
@Table(name = "ai_audios")
data class AiAudioEntity(
    val language: LanguageType,
    val aiVoiceType: AiVoiceType,
    val content: String,
    val url: String,
) : BaseEntity() {
    fun toDomain(): AiAudio =
        AiAudio(
            id = id ?: throw InOutRequireNotNullException("AiAudio id is null", "IORNN_AI_AUDIO_1"),
            language = language,
            aiVoiceType = aiVoiceType,
            content = content,
            url = url,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
}

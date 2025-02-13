package com.inout.apiserver.infrastructure.db.word

import com.inout.apiserver.base.alias.AiAudioId
import com.inout.apiserver.base.enums.AiVoiceType
import com.inout.apiserver.base.enums.LanguageType
import com.inout.apiserver.infrastructure.db.TimestampedEntity
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "ai_audios")
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
) : TimestampedEntity()

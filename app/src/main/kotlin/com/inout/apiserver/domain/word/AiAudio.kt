package com.inout.apiserver.domain.word

import com.inout.apiserver.base.enums.AiVoiceType
import com.inout.apiserver.base.enums.LanguageType
import java.time.Instant

data class AiAudio(
    val id: Long,
    val language: LanguageType,
    val aiVoiceType: AiVoiceType,
    val content: String,
    val url: String,
    val createdAt: Instant? = null,
    val updatedAt: Instant? = null,
)

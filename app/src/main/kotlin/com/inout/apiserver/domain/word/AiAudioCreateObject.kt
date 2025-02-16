package com.inout.apiserver.domain.word

import com.inout.apiserver.base.enums.AiVoiceType
import com.inout.apiserver.base.enums.LanguageType

data class AiAudioCreateObject(
    val language: LanguageType,
    val aiVoiceType: AiVoiceType,
    val content: String,
    val directory: String,
)

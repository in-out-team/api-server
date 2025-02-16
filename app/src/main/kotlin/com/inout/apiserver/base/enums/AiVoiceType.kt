package com.inout.apiserver.base.enums

import com.aallam.openai.api.audio.Voice

enum class AiVoiceType {
    ALLOY,
    ;

    fun toOpenAIVoice() =
        when (this) {
            ALLOY -> Voice.Alloy
            else -> throw IllegalArgumentException("Unknown OpenAI voice type: $this")
        }
}

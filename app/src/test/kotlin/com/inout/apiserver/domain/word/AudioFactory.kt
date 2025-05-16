package com.inout.apiserver.domain.word

import com.inout.apiserver.base.enums.AiVoiceType
import com.inout.apiserver.base.enums.LanguageType
import com.inout.apiserver.infrastructure.mongo.word.MongoAiAudio
import com.inout.apiserver.infrastructure.mongo.word.MongoAiAudioRepository
import org.springframework.stereotype.Component

@Component
class AudioFactory(
    private val aiAudioRepository: MongoAiAudioRepository,
) {
    fun createAudio(
        language: LanguageType = LanguageType.ENGLISH,
        aiVoiceType: AiVoiceType = AiVoiceType.ALLOY,
        content: String = "Let's start a conversation about the word 'book'.",
        directory: String = "static/audio/english/ai_audios_f7365d23-b387-4b79-844d-a5f9f35b49cf.alloy.mp3",
    ): MongoAiAudio =
        aiAudioRepository
            .save(
                MongoAiAudio(
                    language = language,
                    voiceType = aiVoiceType,
                    content = content,
                    directory = directory,
                ),
            )
}

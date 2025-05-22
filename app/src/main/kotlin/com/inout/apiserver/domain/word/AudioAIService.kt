package com.inout.apiserver.domain.word

import com.inout.apiserver.base.constants.AUDIO_TABLE_NAME
import com.inout.apiserver.base.enums.AiVoiceType
import com.inout.apiserver.base.enums.LanguageType
import com.inout.apiserver.base.service.AudioService
import com.inout.apiserver.infrastructure.mongo.word.AiAudio
import com.inout.apiserver.infrastructure.mongo.word.AiAudioRepository
import com.inout.apiserver.infrastructure.s3.S3Service
import org.springframework.stereotype.Service

@Service
class AudioAIService(
    private val audioService: AudioService,
    private val aiAudioRepository: AiAudioRepository,
    private val s3Service: S3Service,
) {
    fun findOrCreateAudio(
        language: LanguageType,
        content: String,
        voiceType: AiVoiceType = AiVoiceType.ALLOY,
    ): AiAudio {
        aiAudioRepository
            .findByLanguageAndVoiceTypeAndContent(
                language = language,
                voiceType = voiceType,
                content = content,
            )?.let { return it }

        val rawAudio = audioService.fetchSpeechFromText(text = content, requestedVoice = voiceType)
        val uploadedDirectory =
            s3Service.uploadAudio(
                inputStream = rawAudio.inputStream(),
                language = language,
                voiceType = voiceType,
                tableName = AUDIO_TABLE_NAME,
                content = content,
            )

        return aiAudioRepository.save(
            AiAudio.fromCreateObject(
                AiAudioCreateObject(
                    language = language,
                    aiVoiceType = voiceType,
                    content = content,
                    directory = uploadedDirectory,
                ),
            ),
        )
    }
}

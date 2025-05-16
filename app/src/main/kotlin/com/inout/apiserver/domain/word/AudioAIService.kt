package com.inout.apiserver.domain.word

import com.inout.apiserver.base.enums.AiVoiceType
import com.inout.apiserver.base.enums.LanguageType
import com.inout.apiserver.base.service.AudioService
import com.inout.apiserver.infrastructure.mongo.word.MongoAiAudio
import com.inout.apiserver.infrastructure.mongo.word.MongoAiAudioRepository
import com.inout.apiserver.infrastructure.s3.S3Service
import org.springframework.stereotype.Service

@Service
class AudioAIService(
    private val audioService: AudioService,
    private val aiAudioRepository: MongoAiAudioRepository,
    private val s3Service: S3Service,
) {
    fun findOrCreateAudio(
        language: LanguageType,
        content: String,
    ): MongoAiAudio {
        aiAudioRepository
            .findByLanguageAndVoiceTypeAndContent(
                language = language,
                voiceType = AiVoiceType.ALLOY, // TODO: temp, support multi voice later
                content = content,
            )?.let { return it }

        val requestVoice = AiVoiceType.ALLOY
        val rawAudio = audioService.fetchSpeechFromText(text = content, requestedVoice = requestVoice)
        val uploadedDirectory =
            s3Service.uploadAudio(
                inputStream = rawAudio.inputStream(),
                language = language,
                voiceType = AiVoiceType.ALLOY,
                tableName = "ai_audios", // TODO: should be dynamic?
                content = content,
            )

        return aiAudioRepository.save(
            MongoAiAudio.fromCreateObject(
                AiAudioCreateObject(
                    language = language,
                    aiVoiceType = requestVoice,
                    content = content,
                    directory = uploadedDirectory,
                ),
            ),
        )
    }
}

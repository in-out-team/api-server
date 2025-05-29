package com.inout.apiserver.domain.word

import com.inout.apiserver.base.constants.AUDIO_TABLE_NAME
import com.inout.apiserver.base.dto.AudioDTO
import com.inout.apiserver.base.enums.AiVoiceType
import com.inout.apiserver.base.enums.LanguageType
import com.inout.apiserver.base.service.AudioService
import com.inout.apiserver.infrastructure.blobstore.BlobStoreService
import com.inout.apiserver.infrastructure.mongo.word.AiAudio
import com.inout.apiserver.infrastructure.mongo.word.AiAudioRepository
import org.springframework.stereotype.Service

@Service
class AudioAIService(
    private val audioService: AudioService,
    private val aiAudioRepository: AiAudioRepository,
    private val blobStoreService: BlobStoreService,
) {
    fun findOrCreateAudio(
        language: LanguageType,
        content: String,
        voiceType: AiVoiceType = AiVoiceType.ALLOY,
    ): AudioDTO {
        aiAudioRepository
            .findByLanguageAndVoiceTypeAndContent(
                language = language,
                voiceType = voiceType,
                content = content,
            )?.let { return AudioDTO.of(it) }

        val rawAudio = audioService.fetchSpeechFromText(text = content, requestedVoice = voiceType)
        val uploadedDirectory =
            blobStoreService.uploadAudio(
                inputStream = rawAudio.inputStream(),
                language = language,
                voiceType = voiceType,
                tableName = AUDIO_TABLE_NAME,
                content = content,
            )

        val aiAudio =
            aiAudioRepository.save(
                AiAudio.fromCreateObject(
                    AiAudioCreateObject(
                        language = language,
                        aiVoiceType = voiceType,
                        content = content,
                        directory = uploadedDirectory,
                    ),
                ),
            )

        return AudioDTO.of(aiAudio)
    }
}

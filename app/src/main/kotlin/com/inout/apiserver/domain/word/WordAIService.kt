package com.inout.apiserver.domain.word

import com.inout.apiserver.base.enums.AiVoiceType
import com.inout.apiserver.base.enums.LanguageType
import com.inout.apiserver.base.service.openai.OpenAIService
import com.inout.apiserver.infrastructure.db.word.AiAudio
import com.inout.apiserver.infrastructure.db.word.AiAudioRepository
import com.inout.apiserver.infrastructure.s3.S3Service
import org.springframework.stereotype.Service

@Service
class WordAIService(
    private val openAIService: OpenAIService,
    private val aiAudioRepository: AiAudioRepository,
    private val s3Service: S3Service,
) {
    fun findOrCreateAudio(
        language: LanguageType,
        content: String,
    ): AiAudio {
        aiAudioRepository
            .findByLanguageAndContent(language = language, content = content)
            ?.let { return it }

        val rawAudio = openAIService.fetchSpeechFromText(content)
        val uploadedDirectory =
            s3Service.uploadAudio(
                inputStream = rawAudio.inputStream(),
                language = language,
                voiceType = AiVoiceType.ALLOY,
                tableName = "ai_audios", // TODO: should be dynamic?
                content = content,
            )

        return aiAudioRepository.save(
            AiAudio.fromCreateObject(
                AiAudioCreateObject(
                    language = language,
                    aiVoiceType = AiVoiceType.ALLOY,
                    content = content,
                    directory = uploadedDirectory,
                ),
            ),
        )
    }
}

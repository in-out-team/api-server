package com.inout.apiserver.base.dto

import com.inout.apiserver.base.enums.AiVoiceType
import com.inout.apiserver.base.enums.LanguageType
import com.inout.apiserver.error.InOutRequireNotNullException
import com.inout.apiserver.infrastructure.mongo.word.AiAudio
import org.bson.types.ObjectId

data class AudioDTO(
    val id: ObjectId,
    val language: LanguageType,
    val voiceType: AiVoiceType,
    val content: String,
    val directory: String,
) {
    companion object {
        fun of(aiAudio: AiAudio): AudioDTO =
            AudioDTO(
                id =
                    aiAudio.id ?: throw InOutRequireNotNullException(
                        message = "Audio ID cannot be null",
                        code = "IORNN_AUDIO_1",
                    ),
                language = aiAudio.language,
                voiceType = aiAudio.voiceType,
                content = aiAudio.content,
                directory = aiAudio.directory,
            )
    }
}

package com.inout.apiserver.infrastructure.blobstore

import com.inout.apiserver.base.enums.AiVoiceType
import com.inout.apiserver.base.enums.LanguageType
import java.io.InputStream

interface BlobStoreService {
    companion object {
        /**
         * audio directory rule:
         * - static/audio/{{language}}/{{rule}}.{{audio speaker}}.mp3
         * - rule?:
         *   - table name + UUID
         *   - ex: ai_speeches_{{uuid}}.alloy.mp3
         */
        const val AUDIO_PREFIX = "static/audio/"
        const val PRE_SIGNED_URL_EXPIRATION = 3600 * 1000 // 1 hour
    }

    /**
     * Uploads an audio file to the blob store.
     * @return the directory where the audio file is stored
     */
    fun uploadAudio(
        inputStream: InputStream,
        language: LanguageType,
        voiceType: AiVoiceType,
        tableName: String,
        content: String,
    ): String

    /**
     * Generates a pre-signed URL for the audio file.
     * @return the pre-signed URL for the audio file
     */
    fun getAudioUrl(directory: String): String

    fun getAudioDirectory(
        language: LanguageType,
        tableName: String,
        content: String,
        voiceType: AiVoiceType,
    ): String
}

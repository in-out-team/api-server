package com.inout.apiserver.base.service

import com.inout.apiserver.base.enums.AiVoiceType
import com.inout.apiserver.base.enums.LanguageType
import org.springframework.web.multipart.MultipartFile

interface AudioService {
    fun fetchTextFromSpeech(
        file: MultipartFile,
        language: LanguageType,
    ): String

    fun fetchSpeechFromText(
        text: String,
        requestedVoice: AiVoiceType,
    ): ByteArray
}

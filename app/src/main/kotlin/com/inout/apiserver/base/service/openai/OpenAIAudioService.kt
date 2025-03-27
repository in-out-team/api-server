package com.inout.apiserver.base.service.openai

import com.aallam.openai.api.audio.AudioResponseFormat
import com.aallam.openai.api.audio.SpeechRequest
import com.aallam.openai.api.audio.SpeechResponseFormat
import com.aallam.openai.api.audio.TranscriptionRequest
import com.aallam.openai.api.file.FileSource
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.inout.apiserver.base.enums.AiVoiceType
import com.inout.apiserver.base.enums.LanguageType
import com.inout.apiserver.base.service.AudioService
import com.inout.apiserver.error.BadRequestException
import kotlinx.coroutines.runBlocking
import okio.source
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile

@Component
class OpenAIAudioService(
    private val openAI: OpenAI,
) : AudioService {
    private val transcriptionModel = ModelId("whisper-1")
    private val transcriptionResponseFormat = AudioResponseFormat.Text
    private val speechModel = ModelId("tts-1")
    private val speechResponseFormat = SpeechResponseFormat.Mp3

    override fun fetchTextFromSpeech(
        file: MultipartFile,
        language: LanguageType,
    ): String {
        if (file.isEmpty) {
            throw BadRequestException(message = "Audio file cannot be empty", code = "OPENAI_001")
        }
        if (file.contentType != "audio/mpeg") {
            throw BadRequestException(message = "Content type must be audio/mpeg", code = "OPENAI_002")
        }

        val transcriptionRequest =
            TranscriptionRequest(
                audio = FileSource(name = "request.mp3", source = file.inputStream.source()),
                model = transcriptionModel,
                language = language.twoLetterCode,
                responseFormat = transcriptionResponseFormat,
            )
        val transcription = runBlocking { openAI.transcription(transcriptionRequest) }
        return transcription.text
    }

    override fun fetchSpeechFromText(
        text: String,
        requestedVoice: AiVoiceType,
    ): ByteArray {
        val speechRequest =
            SpeechRequest(
                model = speechModel,
                input = text,
                voice = requestedVoice.toOpenAIVoice(),
                responseFormat = speechResponseFormat,
            )
        return runBlocking { openAI.speech(speechRequest) }
    }
}

package com.inout.apiserver.interfaces.web.v1

import com.inout.apiserver.base.enums.LanguageType
import com.inout.apiserver.base.service.AudioService
import com.inout.apiserver.interfaces.web.v1.response.SpeechToTextResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.parameters.RequestBody
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/v1/ai")
class AiController(
    private val audioService: AudioService,
) {
    @PostMapping(
        value = ["/speech-to-text"],
        consumes = [MediaType.MULTIPART_FORM_DATA_VALUE],
    )
    @Operation(
        summary = "Speech to Text 기능을 제공합니다. (영문 지원)",
        description = "mp3 파일을 텍스트로 변환합니다.",
        requestBody =
            RequestBody(
                description = "mp3 파일",
                required = true,
                useParameterTypeSchema = true,
            ),
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "성공",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = Schema(implementation = SpeechToTextResponse::class),
                    ),
                ],
            ),
            ApiResponse(
                responseCode = "400",
                description = "Bad Request, 파일 오류가 있을 경우 (OPENAI_001: 파일 미제공, OPENAI_002: 파일 형식 오류)",
            ),
        ],
    )
    fun speechToText(
        @RequestPart("file") file: MultipartFile,
    ): ResponseEntity<SpeechToTextResponse> =
        ResponseEntity.ok(SpeechToTextResponse(audioService.fetchTextFromSpeech(file, LanguageType.ENGLISH)))
}

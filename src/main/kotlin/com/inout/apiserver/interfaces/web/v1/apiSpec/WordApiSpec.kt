package com.inout.apiserver.interfaces.web.v1.apiSpec

import com.inout.apiserver.error.ConflictException
import com.inout.apiserver.interfaces.web.v1.request.CreateWordRequest
import com.inout.apiserver.interfaces.web.v1.request.ReadWordRequest
import com.inout.apiserver.interfaces.web.v1.response.WordResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.parameters.RequestBody as SwaggerRequestBody
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

interface WordApiSpec {
    @PostMapping
    @Operation(
        summary = "글로벌 사전 단어 등록",
        description = "글로벌 사전 단어 등록을 요청합니다.",
        requestBody = SwaggerRequestBody(
            description = "단어 생성 요청값",
            required = true,
            content = [
                Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = CreateWordRequest::class)
                )
            ]
        ),
        responses = [
            ApiResponse(
                responseCode = "201",
                description = "글로벌 사전에 단어 등록 성공",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = WordResponse::class)
                    )
                ]
            ),
            ApiResponse(
                responseCode = "409",
                description = "글로벌 사전에 단어 등록 실패 (code: WORD_1) - 이미 존재하는 단어",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = ConflictException::class),
                    )
                ]
            ),
        // TODO: add more error codes (ex. in case openai service is down, or returns unexpected output)
        ]
    )
    fun createWord(@RequestBody request: CreateWordRequest): ResponseEntity<WordResponse>

    @GetMapping
    @Operation(
        summary = "글로벌 사전 단어 조회",
        description = "글로벌 사전 단어 조회를 요청합니다.",
        requestBody = SwaggerRequestBody(
            description = "단어 조회 요청값",
            required = true,
            content = [
                Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ReadWordRequest::class)
                )
            ]
        ),
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "글로벌 사전에 단어 조회 성공",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = WordResponse::class)
                    )
                ]
            ),
            ApiResponse(
                responseCode = "404",
                description = "글로벌 사전에 단어 조회 실패 (code: WORD_2) - 단어가 존재하지 않음",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = ConflictException::class),
                    )
                ]
            ),
        ]
    )
    fun readWord(@RequestBody request: ReadWordRequest): ResponseEntity<WordResponse>
}
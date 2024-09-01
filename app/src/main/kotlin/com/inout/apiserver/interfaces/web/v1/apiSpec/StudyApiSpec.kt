package com.inout.apiserver.interfaces.web.v1.apiSpec

import com.inout.apiserver.domain.user.User
import com.inout.apiserver.error.HttpException
import com.inout.apiserver.interfaces.web.v1.request.CreateStudyRequest
import com.inout.apiserver.interfaces.web.v1.response.ResponsePaginationWrapper
import com.inout.apiserver.interfaces.web.v1.response.StudyWordResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.media.SchemaProperty
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.parameters.RequestBody as SwaggerRequestBody
import jakarta.validation.Valid
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import kotlin.reflect.KClass

interface StudyApiSpec {
    @PostMapping
    @Operation(
        summary = "단어장에 단어 추가",
        description = "단어장에 학습할 단어를 추가합니다.",
        requestBody = SwaggerRequestBody(
            description = "학습 단어 추가 요청값",
            required = true,
            content = [
                Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = CreateStudyRequest::class)
                )
            ]
        ),
        responses = [
            ApiResponse(
                responseCode = "201",
                description = "학습 단어 추가 성공",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = StudyWordResponse::class)
                    )
                ]
            ),
            ApiResponse(
                responseCode = "404",
                description = "학습 단어 추가 실패 (code: WORD_2) - 존재하지 않는 단어",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = HttpException::class),
                    )
                ]
            ),
            ApiResponse(
                responseCode = "409",
                description = "학습 단어 추가 실패 (code: STUDY_1) - 이미 단어장에 추가된 단어",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = HttpException::class),
                    )
                ]
            ),
        ]
    )
    fun createStudy(
        @RequestBody @Valid request: CreateStudyRequest,
        @Parameter(hidden = true) user: User
    ): ResponseEntity<StudyWordResponse>

    @GetMapping
    @Operation(
        summary = "학습 단어 조회",
        description = "요청자의 단어장에 있는 학습 단어를 조회합니다.",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "학습 단어 조회 성공",
                useReturnTypeSchema = true,
            ),
        ]
    )
    fun getStudies(
        @Parameter(hidden = true) user: User,
        @Parameter(hidden = true) pageable: Pageable,
    ): ResponseEntity<ResponsePaginationWrapper<StudyWordResponse>>
}

fun <T : Any> test(t: T): KClass<out T> = t::class

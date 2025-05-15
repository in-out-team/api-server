package com.inout.apiserver.interfaces.web.v1.apiSpec

import com.inout.apiserver.error.HttpException
import com.inout.apiserver.infrastructure.db.user.User
import com.inout.apiserver.interfaces.web.v1.request.RateStudyWordRequest
import com.inout.apiserver.interfaces.web.v1.response.StudyWordResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import io.swagger.v3.oas.annotations.parameters.RequestBody as SwaggerRequestBody

interface StudyApiSpec {
    @PostMapping("/rate")
    @Operation(
        summary = "학습 단어 평가",
        description = "학습 단어를 평가합니다.",
        requestBody =
            SwaggerRequestBody(
                description = "학습 단어 평가 요청값",
                required = true,
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = RateStudyWordRequest::class),
                    ),
                ],
            ),
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "학습 단어 평가 성공",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = StudyWordResponse::class),
                    ),
                ],
            ),
            ApiResponse(
                responseCode = "404",
                description =
                    "학습 단어 평가 실패 (code: STUDY_2) - 존재하지 않는 학습 단어<br/>" +
                        "학습 단어 평가 실패 (code: STUDY_4) - 존재하지 않는 학습셋",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = HttpException::class),
                    ),
                ],
            ),
            ApiResponse(
                responseCode = "409",
                description = "학습 단어 평가 실패 (code: STUDY_7) - 이미 평가된 학습 단어",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = HttpException::class),
                    ),
                ],
            ),
        ],
    )
    fun rateStudyWord(
        @Parameter(hidden = true) user: User,
        @RequestBody @Valid request: RateStudyWordRequest,
    ): ResponseEntity<StudyWordResponse>
}

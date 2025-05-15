package com.inout.apiserver.interfaces.web.v1.apiSpec

import com.inout.apiserver.error.HttpException
import com.inout.apiserver.infrastructure.db.user.User
import com.inout.apiserver.interfaces.web.v1.request.RateStudyWordRequest
import com.inout.apiserver.interfaces.web.v1.response.DailyStudySetResponse
import com.inout.apiserver.interfaces.web.v1.response.StudyWordResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import java.time.LocalDate
import io.swagger.v3.oas.annotations.parameters.RequestBody as SwaggerRequestBody

interface StudyApiSpec {
    @GetMapping("/daily")
    @Operation(
        summary = "일일 학습셋 조회",
        description = "요청자의 일일 학습셋을 조회합니다.",
        parameters = [
            Parameter(
                name = "date",
                description = "조회할 일자",
                required = true,
                example = "2024-09-29",
            ),
        ],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "일일 학습 진행 상황 조회 성공",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = DailyStudySetResponse::class),
                    ),
                ],
            ),
        ],
    )
    fun getDailyStudySet(
        @Parameter(hidden = true) user: User,
        @Parameter(name = "date", required = true) date: LocalDate,
    ): ResponseEntity<DailyStudySetResponse>

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

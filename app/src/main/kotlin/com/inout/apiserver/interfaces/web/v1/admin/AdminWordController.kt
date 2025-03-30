package com.inout.apiserver.interfaces.web.v1.admin

import com.inout.apiserver.application.word.CreateSentenceApplication
import com.inout.apiserver.application.word.CreateWordApplication
import com.inout.apiserver.application.word.admin.AddWordDefinitionApplication
import com.inout.apiserver.application.word.admin.ApproveWordDefinitionApplication
import com.inout.apiserver.application.word.admin.CreateWordManualApplication
import com.inout.apiserver.error.HttpException
import com.inout.apiserver.interfaces.web.v1.request.AddWordDefinitionRequest
import com.inout.apiserver.interfaces.web.v1.request.CreateWordRequest
import com.inout.apiserver.interfaces.web.v1.response.WordResponse
import com.inout.apiserver.interfaces.web.v1.response.WordWithDefinitionsResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import jakarta.validation.Valid
import org.jobrunr.scheduling.JobScheduler
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/admin/v1/words")
class AdminWordController(
    private val createWordApplication: CreateWordApplication,
    private val createSentenceApplication: CreateSentenceApplication,
    private val createWordManualApplication: CreateWordManualApplication,
    private val addWordDefinitionApplication: AddWordDefinitionApplication,
    private val approveWordDefinitionApplication: ApproveWordDefinitionApplication,
    private val jobScheduler: JobScheduler,
) {
    @PostMapping
    @Operation(
        summary = "사전 단어 등록 (AI)",
        description = "사전 단어 등록을 요청합니다.",
        requestBody =
            io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "단어 생성 요청값",
                required = true,
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = CreateWordRequest::class),
                    ),
                ],
            ),
        responses = [
            ApiResponse(
                responseCode = "201",
                description = "사전 단어 등록 성공",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = WordResponse::class),
                    ),
                ],
            ),
            ApiResponse(
                responseCode = "400",
                description = "사전 정의를 찾을 수 없음 (code: WORD_3)",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = HttpException::class),
                    ),
                ],
            ),
            ApiResponse(
                responseCode = "409",
                description = "사전 단어 등록 실패 (code: WORD_1) - 이미 존재하는 단어",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = HttpException::class),
                    ),
                ],
            ),
        ],
    )
    fun createWord(
        @RequestBody @Valid request: CreateWordRequest,
    ): ResponseEntity<WordResponse> {
        val result =
            createWordApplication.run(
                CreateWordApplication.Request(
                    name = request.name,
                    fromLanguage = request.fromLanguage,
                    toLanguage = request.toLanguage,
                ),
            )
        jobScheduler.enqueue { createSentenceApplication.run(CreateSentenceApplication.Request(wordId = result.word.id!!)) }

        return ResponseEntity(
            WordResponse.of(result.word),
            CREATED,
        )
    }

    @PostMapping("/manual")
    @Operation(
        summary = "사전 단어 수동 등록",
        description = "사전 단어 수동 등록을 요청합니다.",
        requestBody =
            io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "단어 생성 요청값",
                required = true,
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = CreateWordRequest::class),
                    ),
                ],
            ),
        responses = [
            ApiResponse(
                responseCode = "201",
                description = "사전 단어 등록 성공",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = WordResponse::class),
                    ),
                ],
            ),
            ApiResponse(
                responseCode = "409",
                description = "사전 단어 등록 실패 (code: WORD_1) - 이미 존재하는 단어",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = HttpException::class),
                    ),
                ],
            ),
        ],
    )
    fun createWordManual(
        @RequestBody @Valid request: CreateWordRequest,
    ): ResponseEntity<WordResponse> {
        val result =
            createWordManualApplication.run(
                CreateWordManualApplication.Request(
                    name = request.name,
                    fromLanguage = request.fromLanguage,
                    toLanguage = request.toLanguage,
                ),
            )

        return ResponseEntity(
            WordResponse.of(result.word),
            CREATED,
        )
    }

    @PostMapping("/{wordId}/definitions")
    @Operation(
        summary = "사전 단어 정의 추가",
        description = "사전 단어 정의 추가를 요청합니다.",
        requestBody =
            io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "단어 정의 추가 요청값",
                required = true,
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = AddWordDefinitionRequest::class),
                    ),
                ],
            ),
        responses = [
            ApiResponse(
                responseCode = "201",
                description = "사전 단어 정의 추가 성공",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = WordWithDefinitionsResponse::class),
                    ),
                ],
            ),
            ApiResponse(
                responseCode = "404",
                description = "사전 정의를 찾을 수 없음 (code: WORD_4)",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = HttpException::class),
                    ),
                ],
            ),
            ApiResponse(
                responseCode = "409",
                description = "사전 단어 정의 추가 실패 (code: WORD_5) - 이미 존재하는 단어 정의",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = HttpException::class),
                    ),
                ],
            ),
        ],
    )
    fun addDefinitionToWord(
        @RequestBody @Valid request: AddWordDefinitionRequest,
        @PathVariable wordId: Long,
    ): ResponseEntity<WordWithDefinitionsResponse> {
        val result =
            addWordDefinitionApplication.run(
                AddWordDefinitionApplication.Request(
                    wordId = wordId,
                    lexicalCategory = request.lexicalCategory,
                    meaning = request.meaning,
                    preContext = request.preContext,
                ),
            )

        return ResponseEntity(
            WordWithDefinitionsResponse.of(result.word),
            CREATED,
        )
    }

    @PatchMapping("/{wordId}/definitions/{wordDefinitionId}/approve")
    @Operation(
        summary = "사전 단어 정의 승인",
        description = "사전 단어 정의 승인을 요청합니다.",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "사전 단어 정의 승인 성공",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = WordWithDefinitionsResponse::class),
                    ),
                ],
            ),
            ApiResponse(
                responseCode = "400",
                description = "사전 단어 정의 승인 실패 (code: WORD_6) - 승인 할 수 없는 상태",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = HttpException::class),
                    ),
                ],
            ),
            ApiResponse(
                responseCode = "404",
                description = "사전 정의를 찾을 수 없음 (code: WORD_4)",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = HttpException::class),
                    ),
                ],
            ),
        ],
    )
    fun approveWordDefinition(
        @PathVariable wordId: Long,
        @PathVariable wordDefinitionId: Long,
    ): ResponseEntity<WordWithDefinitionsResponse> {
        val result =
            approveWordDefinitionApplication.run(
                ApproveWordDefinitionApplication.Request(
                    wordId = wordId,
                    wordDefinitionId = wordDefinitionId,
                ),
            )

        return ResponseEntity.ok(WordWithDefinitionsResponse.of(result.word))
    }
}

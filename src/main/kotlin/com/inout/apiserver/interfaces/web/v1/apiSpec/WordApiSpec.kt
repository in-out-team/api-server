package com.inout.apiserver.interfaces.web.v1.apiSpec

import com.inout.apiserver.base.enums.LexicalCategoryType
import com.inout.apiserver.error.HttpException
import com.inout.apiserver.interfaces.web.v1.request.CreateWordRequest
import com.inout.apiserver.interfaces.web.v1.request.ReadWordRequest
import com.inout.apiserver.interfaces.web.v1.response.ResponsePaginationWrapper
import com.inout.apiserver.interfaces.web.v1.response.WordDefinitionResponse
import com.inout.apiserver.interfaces.web.v1.response.WordResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.media.SchemaProperty
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springframework.data.domain.Pageable
import io.swagger.v3.oas.annotations.parameters.RequestBody as SwaggerRequestBody
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam

interface WordApiSpec {
    @PostMapping
    @Operation(
        summary = "사전 단어 등록",
        description = "사전 단어 등록을 요청합니다.",
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
                description = "사전에 단어 등록 성공",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = WordResponse::class)
                    )
                ]
            ),
            ApiResponse(
                responseCode = "409",
                description = "사전에 단어 등록 실패 (code: WORD_1) - 이미 존재하는 단어",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = HttpException::class),
                    )
                ]
            ),
        // TODO: add more error codes (ex. in case openai service is down, or returns unexpected output)
        ]
    )
    fun createWord(@RequestBody request: CreateWordRequest): ResponseEntity<WordResponse>

    @GetMapping
    @Operation(
        deprecated = true,
        summary = "사전 단어 조회 (deprecated, /words/definitions로 대체)",
        description = "사전 단어 조회를 요청합니다.",
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
                description = "사전에 단어 조회 성공",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = WordResponse::class)
                    )
                ]
            ),
            ApiResponse(
                responseCode = "404",
                description = "사전에 단어 조회 실패 (code: WORD_2) - 단어가 존재하지 않음",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = HttpException::class),
                    )
                ]
            ),
        ]
    )
    fun readWord(@RequestBody request: ReadWordRequest): ResponseEntity<WordResponse>


    @GetMapping("/definitions")
    @Operation(
        summary = "사전 단어 조회, prefix와 lexicalCategoryType으로 필터링 가능",
        description = "사전 단어 정의 조회를 요청합니다.",
        parameters = [
            Parameter(
                name = "pagable",
                description = "페이지네이션 정보",
                required = true,
                schema = Schema(implementation = Pageable::class)
            ),
            Parameter(
                name = "prefix",
                description = "단어 prefix",
                required = true,
                schema = Schema(implementation = String::class)
            ),
            Parameter(
                name = "lexicalCategoryType",
                description = "단어 품사",
                required = false,
                schema = Schema(implementation = LexicalCategoryType::class)
            )
        ],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "사전에 단어 정의 조회 성공",
                content = [
                    Content(
                        mediaType = "application/json",
                        schemaProperties = [
                            // TODO: this is too verbose, consider using a custom schema
                            SchemaProperty(
                                name = "data",
                                schema = Schema(implementation = WordDefinitionResponse::class)
                            ),
                            SchemaProperty(
                                name = "total",
                                schema = Schema(implementation = Integer::class)
                            ),
                            SchemaProperty(
                                name = "page",
                                schema = Schema(implementation = Integer::class)
                            ),
                            SchemaProperty(
                                name = "size",
                                schema = Schema(implementation = Integer::class)
                            ),
                        ]
                    )
                ]
            ),
        ]
    )
    fun readWordDefinitionsWithMatchingPrefix(
        pageable: Pageable,
        @RequestParam(required = true)
        prefix: String,
        @RequestParam(required = false)
        lexicalCategoryType: LexicalCategoryType?
    ): ResponseEntity<ResponsePaginationWrapper<WordDefinitionResponse>>
}
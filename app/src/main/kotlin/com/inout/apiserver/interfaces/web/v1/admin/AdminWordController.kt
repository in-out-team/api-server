package com.inout.apiserver.interfaces.web.v1.admin

import com.inout.apiserver.application.word.AdminReadWordsApplication
import com.inout.apiserver.application.word.CreateSentenceApplication
import com.inout.apiserver.application.word.CreateWordApplication
import com.inout.apiserver.application.word.admin.AddWordDefinitionApplication
import com.inout.apiserver.application.word.admin.ApproveWordDefinitionApplication
import com.inout.apiserver.application.word.admin.CreateWordManualApplication
import com.inout.apiserver.application.word.admin.DeleteWordDefinitionApplication
import com.inout.apiserver.base.enums.LanguageType
import com.inout.apiserver.base.enums.LexicalCategoryType
import com.inout.apiserver.config.web.RequestMongoUser
import com.inout.apiserver.error.HttpException
import com.inout.apiserver.infrastructure.mongo.user.MongoUser
import com.inout.apiserver.interfaces.web.v1.request.AddWordDefinitionRequest
import com.inout.apiserver.interfaces.web.v1.request.CreateWordRequest
import com.inout.apiserver.interfaces.web.v1.response.MongoWordResponse
import com.inout.apiserver.interfaces.web.v1.response.MongoWordWithDefinitionsResponse
import com.inout.apiserver.interfaces.web.v1.response.ResponsePaginationWrapper
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import jakarta.validation.Valid
import org.bson.types.ObjectId
import org.jobrunr.scheduling.JobScheduler
import org.springdoc.core.converters.models.PageableAsQueryParam
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.HttpStatus.OK
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/admin/v1/words")
class AdminWordController(
    private val createWordApplication: CreateWordApplication,
    private val createSentenceApplication: CreateSentenceApplication,
    private val createWordManualApplication: CreateWordManualApplication,
    private val addWordDefinitionApplication: AddWordDefinitionApplication,
    private val approveWordDefinitionApplication: ApproveWordDefinitionApplication,
    private val deleteWordDefinitionApplication: DeleteWordDefinitionApplication,
    private val adminReadWordsApplication: AdminReadWordsApplication,
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
                        schema = Schema(implementation = MongoWordResponse::class),
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
    ): ResponseEntity<MongoWordResponse> {
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
            MongoWordResponse.of(result.word),
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
                        schema = Schema(implementation = MongoWordResponse::class),
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
    ): ResponseEntity<MongoWordResponse> {
        val result =
            createWordManualApplication.run(
                CreateWordManualApplication.Request(
                    name = request.name,
                    fromLanguage = request.fromLanguage,
                    toLanguage = request.toLanguage,
                ),
            )

        return ResponseEntity(
            MongoWordResponse.of(result.word),
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
                        schema = Schema(implementation = MongoWordWithDefinitionsResponse::class),
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
        @PathVariable wordId: ObjectId,
    ): ResponseEntity<MongoWordWithDefinitionsResponse> {
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
            MongoWordWithDefinitionsResponse.of(result.word),
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
                        schema = Schema(implementation = MongoWordWithDefinitionsResponse::class),
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
        @PathVariable wordId: ObjectId,
        @PathVariable wordDefinitionId: ObjectId,
    ): ResponseEntity<MongoWordWithDefinitionsResponse> {
        val result =
            approveWordDefinitionApplication.run(
                ApproveWordDefinitionApplication.Request(
                    wordId = wordId,
                    wordDefinitionId = wordDefinitionId,
                ),
            )

        jobScheduler.enqueue { createSentenceApplication.createSentencesFor(wordId, wordDefinitionId) }

        return ResponseEntity.ok(MongoWordWithDefinitionsResponse.of(result.word))
    }

    @DeleteMapping("/{wordId}/definitions/{wordDefinitionId}")
    @Operation(
        summary = "사전 단어 정의 삭제",
        description = "사전 단어 정의 삭제를 요청합니다.",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "사전 단어 정의 삭제 성공",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = MongoWordWithDefinitionsResponse::class),
                    ),
                ],
            ),
            ApiResponse(
                responseCode = "400",
                description = "사전 단어 정의 삭제 실패 (code: WORD_7) - 삭제 할 수 없는 상태",
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
    fun deleteWordDefinition(
        @PathVariable wordId: ObjectId,
        @PathVariable wordDefinitionId: ObjectId,
    ): ResponseEntity<MongoWordWithDefinitionsResponse> {
        val result =
            deleteWordDefinitionApplication.run(
                DeleteWordDefinitionApplication.Request(
                    wordId = wordId,
                    wordDefinitionId = wordDefinitionId,
                ),
            )

        return ResponseEntity(MongoWordWithDefinitionsResponse.of(result.word), OK)
    }

    @GetMapping("/definitions")
    @PageableAsQueryParam
    @Operation(
        summary = "사전 단어 조회, prefix와 lexicalCategoryType으로 필터링 가능",
        description = "사전 단어 정의 조회를 요청합니다.",
        parameters = [
            Parameter(
                name = "fromLanguage",
                description = "영한사전 기준 영어에 해당되는 값. ex) apple의 뜻을 한글로 알고싶을 경우 ENGLISH로 제공",
                required = true,
                schema =
                    Schema(
                        implementation = LanguageType::class,
                        example = "ENGLISH",
                    ),
            ),
            Parameter(
                name = "toLanguage",
                description = "영한사전 기준 한국어에 해당되는 값. ex) apple의 뜻을 한글로 알고싶을 경우 KOREAN으로 제공",
                required = true,
                schema =
                    Schema(
                        implementation = LanguageType::class,
                        example = "KOREAN",
                    ),
            ),
            Parameter(
                name = "prefix",
                description = "단어 prefix",
                required = true,
                schema = Schema(implementation = String::class),
            ),
            Parameter(
                name = "lexicalCategory",
                description = "단어 품사",
                required = false,
                schema = Schema(implementation = LexicalCategoryType::class),
            ),
        ],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "사전에 단어 정의 조회 성공",
                useReturnTypeSchema = true,
            ),
        ],
    )
    fun readWordsWithMatchingPrefix(
        @Parameter(hidden = true)
        pageable: Pageable,
        @RequestParam(required = true)
        fromLanguage: LanguageType,
        @RequestParam(required = true)
        toLanguage: LanguageType,
        @RequestParam(required = true)
        prefix: String,
        @RequestParam(required = false)
        lexicalCategory: LexicalCategoryType?,
        @Parameter(hidden = true) @RequestMongoUser user: MongoUser,
    ): ResponseEntity<ResponsePaginationWrapper<MongoWordWithDefinitionsResponse>> {
        val result =
            adminReadWordsApplication.run(
                AdminReadWordsApplication.Request(
                    fromLanguage = fromLanguage,
                    toLanguage = toLanguage,
                    prefix = prefix,
                    lexicalCategoryType = lexicalCategory,
                    pageable = pageable,
                ),
            )

        return ResponseEntity.ok(
            ResponsePaginationWrapper(
                data = result.words.map { MongoWordWithDefinitionsResponse.of(it) },
                hasMore = pageable.next().offset < result.totalCount,
                count = result.totalCount,
            ),
        )
    }
}

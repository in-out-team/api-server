package com.inout.apiserver.interfaces.web.v1

import com.inout.apiserver.application.word.ReadSentencesApplication
import com.inout.apiserver.application.word.SelectSentenceApplication
import com.inout.apiserver.application.word.UnselectSentenceApplication
import com.inout.apiserver.config.web.RequestUser
import com.inout.apiserver.domain.user.User
import com.inout.apiserver.error.HttpException
import com.inout.apiserver.interfaces.web.v1.response.SentenceResponse
import com.inout.apiserver.interfaces.web.v1.response.UserSentenceResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.HttpStatus.OK
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/sentences")
class SentenceController(
    private val readSentencesApplication: ReadSentencesApplication,
    private val selectSentenceApplication: SelectSentenceApplication,
    private val unselectSentenceApplication: UnselectSentenceApplication,
) {
    @GetMapping
    @Operation(
        summary = "단어 정의에 해당하는 문장 조회",
        description = "단어 정의에 해당하는 문장을 조회합니다.",
        parameters = [
            Parameter(
                name = "wordDefinitionId",
                description = "단어 정의 ID",
                required = true,
                schema = Schema(implementation = Long::class),
            ),
        ],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "단어 정의에 해당하는 문장 조회 성공",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = SentenceResponse::class),
                    ),
                ],
            ),
            ApiResponse(
                responseCode = "404",
                description = "단어 정의에 해당하는 문장이 없음",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = HttpException::class),
                    ),
                ],
            ),
        ],
    )
    fun readSentencesByWordDefinitionId(
        @RequestParam(required = true) wordDefinitionId: Long,
    ): ResponseEntity<List<SentenceResponse>> {
        val sentences = readSentencesApplication.run(wordDefinitionId)

        return ResponseEntity(
            sentences.map { SentenceResponse.of(it) },
            OK,
        )
    }

    @PostMapping("{id}/select")
    @Operation(
        summary = "사용자 문장 선택",
        description = "단어 정의에 대하여 메인으로 볼 문장을 선택합니다.",
        parameters = [
            Parameter(
                name = "id",
                description = "문장 ID",
                required = true,
                example = "1",
            ),
        ],
        responses = [
            ApiResponse(
                responseCode = "201",
                description = "사용자 문장 선택 성공",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema =
                            Schema(
                                implementation = UserSentenceResponse::class,
                            ),
                    ),
                ],
            ),
            ApiResponse(
                responseCode = "404",
                description = "문장을 찾을 수 없음 (code: SENTENCE_1)",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = HttpException::class),
                    ),
                ],
            ),
            ApiResponse(
                responseCode = "409",
                description = "이미 선택한 문장 (code: SENTENCE_2)",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = HttpException::class),
                    ),
                ],
            ),
        ],
    )
    fun selectSentence(
        @PathVariable id: Long,
        @Parameter(hidden = true) @RequestUser user: User,
    ) = ResponseEntity(
        UserSentenceResponse.of(selectSentenceApplication.run(id, user)),
        CREATED,
    )

    @DeleteMapping("/{id}/unselect")
    @Operation(
        summary = "사용자 문장 선택 해제",
        description = "선택한 문장을 해제합니다.",
        parameters = [
            Parameter(
                name = "id",
                description = "문장 ID",
                required = true,
                example = "1",
            ),
        ],
        responses = [
            ApiResponse(
                responseCode = "204",
                description = "사용자 문장 선택 해제 성공",
            ),
            ApiResponse(
                responseCode = "404",
                description = "문장을 찾을 수 없음 (code: SENTENCE_3)",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = HttpException::class),
                    ),
                ],
            ),
        ],
    )
    fun unselectSentence(
        @PathVariable id: Long,
        @Parameter(hidden = true) @RequestUser user: User,
    ): ResponseEntity<Void> {
        unselectSentenceApplication.run(id, user)
        return ResponseEntity.noContent().build()
    }
}

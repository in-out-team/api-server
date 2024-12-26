package com.inout.apiserver.interfaces.web.v1

import com.inout.apiserver.application.word.GetRandomWritingSentenceApplication
import com.inout.apiserver.application.word.GetReadingSentencesApplication
import com.inout.apiserver.application.word.SelectReadingSentenceApplication
import com.inout.apiserver.application.word.UnselectReadingSentenceApplication
import com.inout.apiserver.config.web.RequestUser
import com.inout.apiserver.domain.user.User
import com.inout.apiserver.error.HttpException
import com.inout.apiserver.interfaces.web.v1.response.SentenceResponse
import com.inout.apiserver.interfaces.web.v1.response.SentencesResponse
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
    private val getReadingSentencesApplication: GetReadingSentencesApplication,
    private val selectReadingSentenceApplication: SelectReadingSentenceApplication,
    private val unselectReadingSentenceApplication: UnselectReadingSentenceApplication,
    private val getRandomWritingSentenceApplication: GetRandomWritingSentenceApplication,
) {
    @GetMapping("/reading")
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
    fun getReadingSentences(
        @RequestParam(required = true) wordDefinitionId: Long,
        @Parameter(hidden = true) @RequestUser user: User,
    ): ResponseEntity<SentencesResponse> {
        val (selectedSentences, notSelectedSentences) = getReadingSentencesApplication.run(wordDefinitionId, user)

        return ResponseEntity(
            SentencesResponse.of(
                selectedSentences = selectedSentences,
                unselectedSentences = notSelectedSentences,
            ),
            OK,
        )
    }

    @PostMapping("/reading/{id}/select")
    @Operation(
        summary = "사용자 예문 문장 선택",
        description = "단어 정의에 대하여 메인으로 볼 예문 문장을 선택합니다.",
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
                description = "사용자 예문 문장 선택 성공",
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
                description = "예문 문장을 찾을 수 없음 (code: SENTENCE_1)",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = HttpException::class),
                    ),
                ],
            ),
            ApiResponse(
                responseCode = "409",
                description = "이미 선택한 예문 문장 (code: SENTENCE_2)",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = HttpException::class),
                    ),
                ],
            ),
        ],
    )
    fun selectReadingSentence(
        @PathVariable id: Long,
        @Parameter(hidden = true) @RequestUser user: User,
    ) = ResponseEntity(
        UserSentenceResponse.of(selectReadingSentenceApplication.run(id, user)),
        CREATED,
    )

    @DeleteMapping("/reading/{id}/unselect")
    @Operation(
        summary = "사용자 예문 문장 선택 해제",
        description = "선택한 예문 문장을 해제합니다.",
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
                description = "사용자 예문 문장 선택 해제 성공",
            ),
            ApiResponse(
                responseCode = "404",
                description = "예문 문장을 찾을 수 없음 (code: SENTENCE_3)",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = HttpException::class),
                    ),
                ],
            ),
        ],
    )
    fun unselectReadingSentence(
        @PathVariable id: Long,
        @Parameter(hidden = true) @RequestUser user: User,
    ): ResponseEntity<Void> {
        unselectReadingSentenceApplication.run(id, user)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/writing/random")
    @Operation(
        summary = "작문 랜덤 문장 조회",
        description = "작문 연습용 랜덤 문장을 조회하며, 학습을 시작한 문장이 3개에 도달하게 될 경우 에러가 발생합니다.",
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
                description = "작문 랜덤 문장 조회 성공",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = SentenceResponse::class),
                    ),
                ],
            ),
            ApiResponse(
                responseCode = "400",
                description = "이미 선택한 문장이 3개 이상인 경우 (code: SENTENCE_6), 모든 문장이 선택된 경우 (code: SENTENCE_7)",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = HttpException::class),
                    ),
                ],
            ),
            ApiResponse(
                responseCode = "404",
                description = "문장이 없는 경우 (code: SENTENCE_1)",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = HttpException::class),
                    ),
                ],
            ),
        ],
    )
    fun getRandomWritingSentence(
        @RequestParam(required = true) wordDefinitionId: Long,
        @Parameter(hidden = true) @RequestUser user: User,
    ): ResponseEntity<SentenceResponse> =
        ResponseEntity(SentenceResponse.of(getRandomWritingSentenceApplication.run(user, wordDefinitionId)), OK)

    @PostMapping("/writing/{id}/feedback")
    @Operation(
        summary = "작문 문장 피드백",
        description = "제출한 작문 문장에 대한 AI 피드백을 제공합니다.",
    )
    fun getWritingSentenceFeedback(
        @PathVariable id: Long,
        @Parameter(hidden = true) @RequestUser user: User,
    ) {
    }
}

package com.inout.apiserver.interfaces.web.v1

import com.inout.apiserver.application.word.GetRandomWritingSentenceApplication
import com.inout.apiserver.application.word.GetReadingSentencesApplication
import com.inout.apiserver.application.word.GetWritingSentenceFeedbackApplication
import com.inout.apiserver.application.word.GetWritingSentenceFeedbacksApplication
import com.inout.apiserver.application.word.SelectReadingSentenceApplication
import com.inout.apiserver.application.word.UnselectReadingSentenceApplication
import com.inout.apiserver.config.web.RequestUser
import com.inout.apiserver.error.HttpException
import com.inout.apiserver.infrastructure.db.user.User
import com.inout.apiserver.interfaces.web.v1.request.GetWritingSentenceFeedbackRequest
import com.inout.apiserver.interfaces.web.v1.response.SentenceResponse
import com.inout.apiserver.interfaces.web.v1.response.SentencesResponse
import com.inout.apiserver.interfaces.web.v1.response.UserSentenceResponse
import com.inout.apiserver.interfaces.web.v1.response.WritingSentenceFeedbackResponse
import com.inout.apiserver.interfaces.web.v1.response.WritingSentenceFeedbacksResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import jakarta.validation.Valid
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.HttpStatus.OK
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import io.swagger.v3.oas.annotations.parameters.RequestBody as SwaggerRequestBody

@RestController
@RequestMapping("/v1/sentences")
class SentenceController(
    private val getReadingSentencesApplication: GetReadingSentencesApplication,
    private val selectReadingSentenceApplication: SelectReadingSentenceApplication,
    private val unselectReadingSentenceApplication: UnselectReadingSentenceApplication,
    private val getRandomWritingSentenceApplication: GetRandomWritingSentenceApplication,
    private val getWritingSentenceFeedbackApplication: GetWritingSentenceFeedbackApplication,
    private val getWritingSentenceFeedbacksApplication: GetWritingSentenceFeedbacksApplication,
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
        val (selectedSentences, notSelectedSentences) =
            getReadingSentencesApplication.run(
                GetReadingSentencesApplication.Request(
                    wordDefinitionId = wordDefinitionId,
                    user = user,
                ),
            )

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
        UserSentenceResponse.of(
            selectReadingSentenceApplication
                .run(
                    SelectReadingSentenceApplication.Request(
                        sentenceId = id,
                        user = user,
                    ),
                ).userSentence,
        ),
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
        unselectReadingSentenceApplication.run(
            UnselectReadingSentenceApplication.Request(
                sentenceId = id,
                user = user,
            ),
        )
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
        ResponseEntity(
            SentenceResponse.of(
                getRandomWritingSentenceApplication
                    .run(
                        GetRandomWritingSentenceApplication.Request(
                            wordDefinitionId = wordDefinitionId,
                            user = user,
                        ),
                    ).sentence,
            ),
            OK,
        )

    @PostMapping("/writing/{id}/feedback")
    @Operation(
        summary = "작문 문장 피드백",
        description = "제출한 작문 문장에 대한 AI 피드백을 제공합니다. 외부 API 요청을 하므로 시간이 소요될 수 있으니 spinner 같은 UI를 제공해주세요.",
        parameters = [
            Parameter(
                name = "id",
                description = "문장 ID",
                required = true,
                example = "1",
            ),
        ],
        requestBody =
            SwaggerRequestBody(
                description = "제출한 작문 문장",
                required = true,
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = GetWritingSentenceFeedbackRequest::class),
                    ),
                ],
            ),
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "작문 문장 피드백 성공",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = WritingSentenceFeedbackResponse::class),
                    ),
                ],
            ),
            ApiResponse(
                responseCode = "400",
                description = "피드백 받은 횟수가 최대 횟수에 도달한 경우 (code: SENTENCE_8)",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = HttpException::class),
                    ),
                ],
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
    fun getWritingSentenceFeedback(
        @PathVariable id: Long,
        @RequestBody @Valid request: GetWritingSentenceFeedbackRequest,
        @Parameter(hidden = true) @RequestUser user: User,
    ) = ResponseEntity(
        WritingSentenceFeedbackResponse(
            getWritingSentenceFeedbackApplication
                .run(
                    GetWritingSentenceFeedbackApplication.Request(
                        user = user,
                        sentenceId = id,
                        submittedContent = request.submittedContent,
                    ),
                ).feedback,
        ),
        OK,
    )

    @GetMapping("/writing/{id}/feedbacks")
    @Operation(
        summary = "작문 문장 피드백 조회",
        description = "제출한 작문 문장에 대해 제공받았던 AI 피드백을 조회합니다.",
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
                responseCode = "200",
                description = "작문 문장 피드백 조회 성공",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = WritingSentenceFeedbacksResponse::class),
                    ),
                ],
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
    fun getWritingSentenceFeedbacks(
        @PathVariable id: Long,
        @Parameter(hidden = true) @RequestUser user: User,
    ) = ResponseEntity(
        WritingSentenceFeedbacksResponse.of(
            getWritingSentenceFeedbacksApplication
                .run(
                    GetWritingSentenceFeedbacksApplication.Request(
                        sentenceId = id,
                        user = user,
                    ),
                ).feedbacks,
        ),
        OK,
    )
}

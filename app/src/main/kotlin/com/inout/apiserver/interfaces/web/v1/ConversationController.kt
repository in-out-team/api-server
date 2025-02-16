package com.inout.apiserver.interfaces.web.v1

import com.inout.apiserver.application.word.GetConversationsApplication
import com.inout.apiserver.application.word.StartConversationApplication
import com.inout.apiserver.config.web.RequestUser
import com.inout.apiserver.error.HttpException
import com.inout.apiserver.infrastructure.db.user.User
import com.inout.apiserver.interfaces.web.v1.request.StartConversationRequest
import com.inout.apiserver.interfaces.web.v1.response.ConversationResponse
import com.inout.apiserver.interfaces.web.v1.response.ResponseListWrapper
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import jakarta.validation.Valid
import org.springframework.http.HttpStatus.OK
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/conversations")
class ConversationController(
    private val getConversationsApplication: GetConversationsApplication,
    private val startConversationApplication: StartConversationApplication,
) {
    @GetMapping
    @Operation(
        summary = "진행중인 대화 목록 조회",
        description = "학습중인 단어에 대한 대화 목록을 조회합니다.",
        parameters = [
            Parameter(
                name = "wordDefinitionId",
                description = "단어 정의 ID",
                required = true,
                example = "1",
            ),
        ],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "대화 목록 조회 성공",
                useReturnTypeSchema = true,
            ),
            ApiResponse(
                responseCode = "400",
                description = "학습중인 단어가 아닌 경우 (code: CONVERSATION_1)",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = Schema(implementation = HttpException::class),
                    ),
                ],
            ),
        ],
    )
    fun getConversations(
        @RequestParam(required = true) wordDefinitionId: Long,
        @Parameter(hidden = true) @RequestUser user: User,
    ): ResponseEntity<ResponseListWrapper<ConversationResponse>> {
        val result =
            getConversationsApplication.run(
                GetConversationsApplication.Request(
                    wordDefinitionId = wordDefinitionId,
                    user = user,
                ),
            )

        return ResponseEntity(
            ResponseListWrapper(
                data = result.conversations.map { ConversationResponse.of(it) },
            ),
            OK,
        )
    }

    @PostMapping
    @Operation(
        summary = "학습 단어에 대한 대화 시작",
        description = "학습중인 단어에 대해 대화를 시작합니다. 기존 시작된 사용자가 대화하지 않은 대화가 있으면 해당 대화를 사용합니다.",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "대화 시작 성공",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = Schema(implementation = ConversationResponse::class),
                    ),
                ],
            ),
            ApiResponse(
                responseCode = "400",
                description = "요청한 단어를 학습중이 아닌 경우 (code: CONVERSATION_1), 이미 선택한 학습 단어로 3개의 대화를 하고있는 경우 (code: CONVERSATION_2)",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = Schema(implementation = HttpException::class),
                    ),
                ],
            ),
        ],
    )
    fun startConversation(
        @RequestBody @Valid request: StartConversationRequest,
        @Parameter(hidden = true) @RequestUser user: User,
    ): ResponseEntity<ConversationResponse> =
        ResponseEntity.ok(
            ConversationResponse.of(
                startConversationApplication
                    .run(
                        StartConversationApplication.Request(
                            wordDefinitionId = request.wordDefinitionId,
                            user = user,
                        ),
                    ).conversation,
            ),
        )
}

package com.inout.apiserver.interfaces.web.v1

import com.inout.apiserver.application.word.GetConversationsApplication
import com.inout.apiserver.config.web.RequestUser
import com.inout.apiserver.domain.user.User
import com.inout.apiserver.interfaces.web.v1.response.ConversationResponse
import com.inout.apiserver.interfaces.web.v1.response.ResponseListWrapper
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/conversations")
class ConversationController(
    private val getConversationsApplication: GetConversationsApplication,
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
        ],
    )
    fun getConversations(
        @RequestParam(required = true) wordDefinitionId: Long,
        @Parameter(hidden = true) @RequestUser user: User,
    ): ResponseEntity<ResponseListWrapper<ConversationResponse>> =
        ResponseEntity.ok(
            ResponseListWrapper(
                data =
                    getConversationsApplication
                        .run(
                            wordDefinitionId,
                            user,
                        ).conversations
                        .map { ConversationResponse.of(it) },
            ),
        )
}

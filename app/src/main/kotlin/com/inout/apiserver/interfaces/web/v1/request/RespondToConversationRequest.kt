package com.inout.apiserver.interfaces.web.v1.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotEmpty

data class RespondToConversationRequest(
    @field:NotEmpty
    @Schema(
        description = "마지막 질의에 대한 사용자 응답 메시지",
        example = "I like to read books.",
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    val responseMessage: String,
)

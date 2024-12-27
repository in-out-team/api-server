package com.inout.apiserver.interfaces.web.v1.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotEmpty

data class GetWritingSentenceFeedbackRequest(
    @field:NotEmpty
    @Schema(
        description = "작성한/피드백을 받을 문장",
        example = "I am a student",
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    val submittedContent: String,
)

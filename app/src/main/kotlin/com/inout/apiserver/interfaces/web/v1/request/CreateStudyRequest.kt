package com.inout.apiserver.interfaces.web.v1.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Min

data class CreateStudyRequest(
    @field:Min(value = 1L, message = "학습 단어 아이디는 1 이상의 숫자로 입력해주세요.")
    @Schema(description = "학습 단어 아이디", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    val wordDefinitionId: Long,
)

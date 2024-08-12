package com.inout.apiserver.interfaces.web.v1.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Size

data class UpdateUserRequest(
    @field:Min(value = 1, message = "아이디는 1 이상의 숫자로 입력해주세요.")
    @Schema(description = "사용자 아이디", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    val id: Long,

    @field:NotEmpty
    @field:Size(min = 2, max = 20, message = "닉네임은 2자 이상 20자 이하로 입력해주세요.")
    @Schema(description = "사용자 닉네임", example = "inout", requiredMode = Schema.RequiredMode.REQUIRED)
    val nickname: String
)

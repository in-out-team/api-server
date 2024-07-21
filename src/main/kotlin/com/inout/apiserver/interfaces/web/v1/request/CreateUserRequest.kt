package com.inout.apiserver.interfaces.web.v1.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Size

data class CreateUserRequest(
    @field:NotEmpty
    @field:Email(message = "올바른 이메일 형식이 아닙니다.", regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}\$")
    @Schema(description = "사용자 이메일", example = "test@1.com", requiredMode = Schema.RequiredMode.REQUIRED)
    val email: String,

    @field:NotEmpty
    @field:Size(min = 8, max = 20, message = "비밀번호는 8자 이상 20자 이하로 입력해주세요.")
    @Schema(description = "사용자 비밀번호", example = "password123", requiredMode = Schema.RequiredMode.REQUIRED)
    val password: String,

    @field:NotEmpty
    @field:Size(min = 2, max = 20, message = "닉네임은 2자 이상 20자 이하로 입력해주세요.")
    @Schema(description = "사용자 닉네임", example = "inout", requiredMode = Schema.RequiredMode.REQUIRED)
    val nickname: String,
)

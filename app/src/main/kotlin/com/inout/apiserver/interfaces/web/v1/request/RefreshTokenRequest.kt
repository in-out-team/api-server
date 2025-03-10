package com.inout.apiserver.interfaces.web.v1.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotEmpty

data class RefreshTokenRequest(
    @field:NotEmpty
    @Schema(
        description = "재발급 할 refresh token",
        example = "abc.def.ghi",
        required = true,
    )
    val refreshToken: String,
)

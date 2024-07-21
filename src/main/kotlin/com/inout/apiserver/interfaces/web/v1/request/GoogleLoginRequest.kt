package com.inout.apiserver.interfaces.web.v1.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotEmpty

data class GoogleLoginRequest(
    @field:NotEmpty
    @Schema(
        description = "구글 로그인으로부터 받은 idToken값",
        example = "abcd.efgh.ijkl",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    val idToken: String,
)

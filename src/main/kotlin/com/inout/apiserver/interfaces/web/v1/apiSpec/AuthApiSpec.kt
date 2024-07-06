package com.inout.apiserver.interfaces.web.v1.apiSpec

import com.inout.apiserver.error.HttpException
import com.inout.apiserver.interfaces.web.v1.request.GoogleLoginRequest
import com.inout.apiserver.interfaces.web.v1.request.UserLoginRequest
import com.inout.apiserver.interfaces.web.v1.response.TokenResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.parameters.RequestBody as SwaggerRequestBody
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

interface AuthApiSpec {
    @PostMapping("/login")
    @Operation(
        summary = "로그인",
        description = "로그인합니다.",
        requestBody = SwaggerRequestBody(
            description = "로그인 요청",
            required = true,
            content = [
                Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = UserLoginRequest::class)
                )
            ]
        ),
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "로그인 성공",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = TokenResponse::class),
                    )
                ]
            ),
            ApiResponse(
                responseCode = "401",
                description = "로그인 실패 (code: Auth_1) - 사용자 정보가 일치하지 않습니다.",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = HttpException::class),
                    )
                ]
            )
        ]
    )
    fun login(@RequestBody request: UserLoginRequest): ResponseEntity<TokenResponse>

    @PostMapping("/login/google")
    @Operation(
        summary = "구글 로그인",
        description = "구글 계정으로 로그인을 시도합니다. ID Token을 전달해야 합니다.",
        requestBody = SwaggerRequestBody(
            description = "구글 로그인 요청",
            required = true,
            content = [
                Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = GoogleLoginRequest::class)
                )
            ]
        ),
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "로그인 성공",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = TokenResponse::class),
                    )
                ]
            )
            // TODO: add fail responses
        ]
    )
    fun googleLogin(@RequestBody request: GoogleLoginRequest): ResponseEntity<TokenResponse>
}

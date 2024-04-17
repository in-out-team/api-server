package com.inout.apiserver.interfaces.web.v1.apiSpec

import com.inout.apiserver.error.ConflictException
import com.inout.apiserver.interfaces.web.v1.request.CreateUserRequest
import com.inout.apiserver.interfaces.web.v1.response.UserResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.parameters.RequestBody
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping

interface UserApiSpec {
    @PostMapping
    @Operation(
        summary = "사용자 생성",
        description = "사용자를 생성합니다.",
        requestBody = RequestBody(
            description = "사용자 생성 요청값",
            required = true,
            content = [
                Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = CreateUserRequest::class)
                )
            ]
        ),
        responses = [
            ApiResponse(
                responseCode = "201",
                description = "사용자 생성 성공",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = UserResponse::class)
                    )
                ]
            ),
            ApiResponse(
                responseCode = "409",
                description = "사용자 생성 실패 (code: USER_1) - 이미 존재하는 사용자",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = ConflictException::class),
                    )
                ]
            )
        ]
    )
    fun createUser(request: CreateUserRequest): ResponseEntity<UserResponse>
}
package com.inout.apiserver.interfaces.web.v1.apiSpec

import com.inout.apiserver.error.ConflictException
import com.inout.apiserver.error.NotFoundException
import com.inout.apiserver.interfaces.web.v1.request.CreateUserRequest
import com.inout.apiserver.interfaces.web.v1.request.UpdateUserRequest
import com.inout.apiserver.interfaces.web.v1.response.UserResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.parameters.RequestBody as SwaggerRequestBody
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

interface UserApiSpec {
    @PostMapping
    @Operation(
        summary = "사용자 생성",
        description = "사용자를 생성합니다.",
        requestBody = SwaggerRequestBody(
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
    fun createUser(@RequestBody request: CreateUserRequest): ResponseEntity<UserResponse>

    @PutMapping
    @Operation(
        summary = "사용자 정보 수정",
        description = "사용자 정보를 수정합니다.",
        requestBody = SwaggerRequestBody(
            description = "사용자 정보 수정 요청값",
            required = true,
            content = [
                Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = UpdateUserRequest::class)
                )
            ]
        ),
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "사용자 정보 수정 성공",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = UserResponse::class)
                    )
                ]
            ),
        ApiResponse(
                responseCode = "404",
                description = "사용자 정보 수정 실패 (code: USER_2) - 사용자를 찾을 수 없음",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = NotFoundException::class),
                    )
                ]
            )
        ]
    )
    fun updateUser(@RequestBody request: UpdateUserRequest): ResponseEntity<UserResponse>


    @GetMapping("/{id}")
    @Operation(
        summary = "사용자 조회",
        description = "사용자를 조회합니다.",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "사용자 조회 성공",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = UserResponse::class)
                    )
                ]
            ),
            ApiResponse(
                responseCode = "404",
                description = "사용자 조회 실패 (code: USER_2) - 사용자를 찾을 수 없음",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = NotFoundException::class),
                    )
                ]
            )
        ]
    )
    fun getUser(@PathVariable id: Long): ResponseEntity<UserResponse>
}
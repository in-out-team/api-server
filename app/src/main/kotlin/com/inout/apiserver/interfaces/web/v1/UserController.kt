package com.inout.apiserver.interfaces.web.v1

import com.inout.apiserver.application.user.CreateUserApplication
import com.inout.apiserver.application.user.ReadUserApplication
import com.inout.apiserver.application.user.UpdateUserApplication
import com.inout.apiserver.config.web.RequestUser
import com.inout.apiserver.error.HttpException
import com.inout.apiserver.infrastructure.mongo.user.User
import com.inout.apiserver.interfaces.web.v1.request.CreateUserRequest
import com.inout.apiserver.interfaces.web.v1.request.UpdateUserRequest
import com.inout.apiserver.interfaces.web.v1.response.UserResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import jakarta.validation.Valid
import org.bson.types.ObjectId
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.HttpStatus.OK
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/users")
class UserController(
    private val createUserApplication: CreateUserApplication,
    private val updateUserApplication: UpdateUserApplication,
    private val readUserApplication: ReadUserApplication,
) {
    @PostMapping
    @Operation(
        summary = "사용자 생성",
        description = "사용자를 생성합니다.",
        requestBody =
            io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "사용자 생성 요청값",
                required = true,
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = CreateUserRequest::class),
                    ),
                ],
            ),
        responses = [
            ApiResponse(
                responseCode = "201",
                description = "사용자 생성 성공",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = UserResponse::class),
                    ),
                ],
            ),
            ApiResponse(
                responseCode = "400",
                description = "사용자 생성 실패 (code: USER_2) - 이메일 & 비밃번호로 사용자 생성을 지원하지 않는 환경",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = HttpException::class),
                    ),
                ],
            ),
            ApiResponse(
                responseCode = "409",
                description = "사용자 생성 실패 (code: USER_1) - 이미 존재하는 사용자",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = HttpException::class),
                    ),
                ],
            ),
        ],
    )
    fun createUser(
        @RequestBody @Valid request: CreateUserRequest,
    ): ResponseEntity<UserResponse> =
        ResponseEntity(
            UserResponse.of(
                user =
                    createUserApplication
                        .run(
                            CreateUserApplication.Request(
                                email = request.email,
                                password = request.password,
                                nickname = request.nickname,
                            ),
                        ).newUser,
            ),
            CREATED,
        )

    @PutMapping
    @Operation(
        summary = "사용자 정보 수정",
        description = "사용자 정보를 수정합니다.",
        requestBody =
            io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "사용자 정보 수정 요청값",
                required = true,
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = UpdateUserRequest::class),
                    ),
                ],
            ),
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "사용자 정보 수정 성공",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = UserResponse::class),
                    ),
                ],
            ),
            ApiResponse(
                responseCode = "404",
                description = "사용자 정보 수정 실패 (code: USER_2) - 사용자를 찾을 수 없음",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = HttpException::class),
                    ),
                ],
            ),
        ],
        // TODO: add fail responses
    )
    fun updateUser(
        @RequestBody @Valid request: UpdateUserRequest,
        @RequestUser user: User,
    ): ResponseEntity<UserResponse> =
        ResponseEntity(
            UserResponse.of(
                user =
                    updateUserApplication
                        .run(
                            UpdateUserApplication.Request(
                                user = user,
                                newNickname = request.nickname,
                                studyLanguage = request.studyLanguage,
                                nativeLanguage = request.nativeLanguage,
                                studyPerDay = request.studyPerDay,
                                timezone = request.timezone,
                            ),
                        ).updatedUser,
            ),
            OK,
        )

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
                        schema = Schema(implementation = UserResponse::class),
                    ),
                ],
            ),
            ApiResponse(
                responseCode = "404",
                description = "사용자 조회 실패 (code: USER_2) - 사용자를 찾을 수 없음",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = HttpException::class),
                    ),
                ],
            ),
            // TODO: add fail responses
        ],
    )
    fun getUser(
        @PathVariable id: ObjectId,
    ): ResponseEntity<UserResponse> =
        ResponseEntity(
            UserResponse.of(
                user = readUserApplication.run(ReadUserApplication.Request(id = id)).user,
            ),
            OK,
        )
}

package com.inout.apiserver.interfaces.web.v1

import com.inout.apiserver.application.auth.EmailPasswordLoginApplication
import com.inout.apiserver.application.auth.GoogleLoginApplication
import com.inout.apiserver.application.user.RefreshTokenApplication
import com.inout.apiserver.error.HttpException
import com.inout.apiserver.interfaces.web.v1.request.GoogleLoginRequest
import com.inout.apiserver.interfaces.web.v1.request.RefreshTokenRequest
import com.inout.apiserver.interfaces.web.v1.request.UserLoginRequest
import com.inout.apiserver.interfaces.web.v1.response.TokenResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/auth")
class AuthController(
    private val emailPasswordLoginApplication: EmailPasswordLoginApplication,
    private val googleLoginApplication: GoogleLoginApplication,
    private val refreshTokenApplication: RefreshTokenApplication,
) {
    @PostMapping("/login")
    @Operation(
        summary = "로그인",
        description = "로그인합니다.",
        requestBody =
            io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "로그인 요청",
                required = true,
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = UserLoginRequest::class),
                    ),
                ],
            ),
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "로그인 성공",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = TokenResponse::class),
                    ),
                ],
            ),
            ApiResponse(
                responseCode = "401",
                description = "로그인 실패 (code: Auth_1) - 사용자 정보가 일치하지 않습니다.",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = HttpException::class),
                    ),
                ],
            ),
            ApiResponse(
                responseCode = "404",
                description = "로그인 실패 (code: USER_2) - 사용자가 존재하지 않습니다.",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = HttpException::class),
                    ),
                ],
            ),
        ],
    )
    fun login(
        @RequestBody @Valid request: UserLoginRequest,
    ): ResponseEntity<TokenResponse> {
        val result =
            emailPasswordLoginApplication
                .run(
                    EmailPasswordLoginApplication.Request(
                        email = request.email,
                        password = request.password,
                    ),
                )
        return ResponseEntity.ok(
            TokenResponse(
                accessToken = result.accessToken,
                refreshToken = result.refreshToken,
            ),
        )
    }

    @PostMapping("/login/google")
    @Operation(
        summary = "구글 로그인",
        description = "구글 계정으로 로그인을 시도합니다.",
        requestBody =
            io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "구글 로그인 요청",
                required = true,
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = GoogleLoginRequest::class),
                    ),
                ],
            ),
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "로그인 성공",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = TokenResponse::class),
                    ),
                ],
            ),
            ApiResponse(
                responseCode = "401",
                description = "로그인 실패 (code: GOOGLE_AUTH_1) - 구글 ID 토큰이 유효하지 않습니다.",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = HttpException::class),
                    ),
                ],
            ),
        ],
    )
    fun googleLogin(
        @RequestBody @Valid request: GoogleLoginRequest,
    ): ResponseEntity<TokenResponse> {
        val result =
            googleLoginApplication
                .run(
                    GoogleLoginApplication.Request(
                        idToken = request.idToken,
                    ),
                )
        return ResponseEntity.ok(
            TokenResponse(
                accessToken = result.accessToken,
                refreshToken = result.refreshToken,
            ),
        )
    }

    @PostMapping("/refresh")
    @Operation(
        summary = "토큰 갱신",
        description = "리프레시 토큰을 이용하여 액세스 토큰을 갱신합니다.",
        requestBody =
            io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "토큰 갱신 요청",
                required = true,
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = RefreshTokenRequest::class),
                    ),
                ],
            ),
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "토큰 갱신 성공",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = TokenResponse::class),
                    ),
                ],
            ),
            ApiResponse(
                responseCode = "401",
                description = "토큰 갱신 실패 (code: Auth_4) - 리프레시 토큰이 유효하지 않음",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = HttpException::class),
                    ),
                ],
            ),
            ApiResponse(
                responseCode = "404",
                description = "토큰 갱신 실패 (code: Auth_2) - 리프레시 토큰이 존재하지 않음",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = HttpException::class),
                    ),
                ],
            ),
        ],
    )
    fun refresh(
        @RequestBody @Valid request: RefreshTokenRequest,
    ): ResponseEntity<TokenResponse> {
        val result =
            refreshTokenApplication
                .run(
                    RefreshTokenApplication.Request(
                        token = request.refreshToken,
                    ),
                )
        return ResponseEntity.ok(
            TokenResponse(
                accessToken = result.accessToken,
                refreshToken = result.refreshToken,
            ),
        )
    }
}

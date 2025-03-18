package com.inout.apiserver.interfaces.web.v1.request

import com.inout.apiserver.base.enums.LanguageType
import com.inout.apiserver.error.BadRequestException
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import java.time.ZoneId

data class UpdateUserRequest(
    @field:Min(value = 1, message = "아이디는 1 이상의 숫자로 입력해주세요.")
    @field:NotNull(message = "아이디는 필수입니다.")
    @Schema(description = "사용자 아이디", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    val id: Long,
    @field:NotEmpty
    @field:NotNull(message = "닉네임은 필수입니다.")
    @field:Size(min = 2, max = 20, message = "닉네임은 2자 이상 20자 이하로 입력해주세요.")
    @Schema(description = "사용자 닉네임", example = "inout", requiredMode = Schema.RequiredMode.REQUIRED)
    val nickname: String,
    @field:NotNull(message = "학습 언어는 필수입니다.")
    @Schema(description = "사용자 학습 언어", example = "ENGLISH", requiredMode = Schema.RequiredMode.REQUIRED)
    val studyLanguage: LanguageType,
    @field:NotNull(message = "모국어는 필수입니다.")
    @Schema(description = "사용자 모국어", example = "KOREAN", requiredMode = Schema.RequiredMode.REQUIRED)
    val nativeLanguage: LanguageType,
    @field:Min(value = 1, message = "하루 학습 갯수는 1 이상입니다.")
    @field:NotNull(message = "하루 학습 갯수는 필수입니다.")
    @Schema(description = "하루 학습 갯수", example = "3", requiredMode = Schema.RequiredMode.REQUIRED)
    val studyPerDay: Int,
    @field:Pattern(
        regexp = "^[A-Za-z_]+/[A-Za-z_]+$",
        message = "유효하지 않은 시간대입니다. 예시: Asia/Seoul, America/New_York",
    )
    @field:NotNull(message = "시간대는 필수입니다.")
    @Schema(description = "사용자 시간대", example = "Asia/Seoul", requiredMode = Schema.RequiredMode.REQUIRED)
    val timezone: String,
) {
    init {
        // study language must be english and native language must be korean
        if (studyLanguage != LanguageType.ENGLISH || nativeLanguage != LanguageType.KOREAN) {
            throw BadRequestException(
                message = "invalid studyLanguage, currently only supports ENGLISH and KOREAN",
                code = "USER_UUR_2",
            )
        }

        try {
            ZoneId.of(timezone)
        } catch (e: Exception) {
            throw BadRequestException(
                message = "invalid timezone",
                code = "USER_UUR_1",
            )
        }
    }
}

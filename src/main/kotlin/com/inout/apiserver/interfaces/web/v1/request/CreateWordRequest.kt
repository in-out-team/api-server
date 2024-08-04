package com.inout.apiserver.interfaces.web.v1.request

import com.inout.apiserver.base.enums.LanguageType
import com.inout.apiserver.error.BadRequestException
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotEmpty

data class CreateWordRequest(
    @NotEmpty
    @Schema(description = "단어 이름", example = "apple", requiredMode = Schema.RequiredMode.REQUIRED)
    val name: String,

    @NotEmpty
    @Schema(
        description = "영한사전 기준 영어에 해당되는 값. ex) apple의 뜻을 한글로 알고싶을 경우 ENGLISH로 제공",
        example = "ENGLISH",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    val fromLanguage: LanguageType,

    @NotEmpty
    @Schema(
        description = "영한사전 기준 한국어에 해당되는 값. ex) apple의 뜻을 한글로 알고싶을 경우 KOREAN으로 제공",
        example = "KOREAN",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    val toLanguage: LanguageType
) {
    init {
        if (fromLanguage == toLanguage) {
            throw BadRequestException(message = "fromLanguage and toLanguage must be different", code = "WORD_CWR_1")
        }
    }
}

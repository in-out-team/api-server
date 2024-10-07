package com.inout.apiserver.interfaces.web.v1.request

import com.inout.apiserver.base.enums.FsrsCardRating
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Min

data class RateStudyWordRequest(
    @field:Min(value = 1L, message = "일일 학습셋 아이디는 1 이상의 숫자로 입력해주세요.")
    @Schema(description = "일일 학습셋 아이디", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    val dailyStudySetId: Long,
    @field:Min(value = 1L, message = "학습 단어 아이디는 1 이상의 숫자로 입력해주세요.")
    @Schema(description = "학습 단어 아이디", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    val studyId: Long,
    @Schema(description = "학습 단어 평가, MANUAL은 사용 불가", example = "GOOD", requiredMode = Schema.RequiredMode.REQUIRED)
    val rating: FsrsCardRating,
) {
    init {
        require(rating != FsrsCardRating.MANUAL) { "MANUAL rating is not allowed" }
    }
}

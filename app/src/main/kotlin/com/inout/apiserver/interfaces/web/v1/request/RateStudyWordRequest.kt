package com.inout.apiserver.interfaces.web.v1.request

import com.inout.apiserver.base.enums.FsrsCardRating
import io.swagger.v3.oas.annotations.media.Schema
import org.bson.types.ObjectId

data class RateStudyWordRequest(
    @Schema(
        description = "일일 학습셋 아이디 (24자리 Hex)",
        example = "60d5ec49e1d3f5312c1e4c6b",
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    val dailyStudySetId: ObjectId,
    @Schema(
        description = "학습 단어 아이디 (24자리 Hex)",
        example = "60d5ec49e1d3f5312c1e4c6b",
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    val studyId: ObjectId,
    @Schema(description = "학습 단어 평가, MANUAL은 사용 불가", example = "GOOD", requiredMode = Schema.RequiredMode.REQUIRED)
    val rating: FsrsCardRating,
) {
    init {
        require(rating != FsrsCardRating.MANUAL) { "MANUAL rating is not allowed" }
    }
}

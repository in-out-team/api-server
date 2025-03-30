package com.inout.apiserver.interfaces.web.v1.request

import com.inout.apiserver.base.enums.LexicalCategoryType
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotEmpty

data class AddWordDefinitionRequest(
    @Schema(description = "추가할 단어 정의의 품사", example = "NOUN", requiredMode = Schema.RequiredMode.REQUIRED)
    val lexicalCategory: LexicalCategoryType,
    @field:NotEmpty
    @Schema(description = "단어 정의", example = "책", requiredMode = Schema.RequiredMode.REQUIRED)
    val meaning: String,
    @field:NotEmpty
    @Schema(
        description = "단어 정의의 문맥",
        example = "정보를 얻거나 즐거움을 얻기 위해 읽는 인쇄물",
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    val preContext: String,
)

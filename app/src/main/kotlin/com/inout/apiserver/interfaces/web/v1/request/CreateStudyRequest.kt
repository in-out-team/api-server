package com.inout.apiserver.interfaces.web.v1.request

import io.swagger.v3.oas.annotations.media.Schema
import org.bson.types.ObjectId

data class CreateStudyRequest(
    @Schema(
        description = "학습 단어 ObjectId (24자리 Hex)",
        example = "60d5ec49e1d3f5312c1e4c6b",
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    val wordDefinitionId: ObjectId,
)

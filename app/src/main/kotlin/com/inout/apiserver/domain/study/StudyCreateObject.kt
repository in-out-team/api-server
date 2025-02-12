package com.inout.apiserver.domain.study

import com.inout.apiserver.base.alias.UserId
import com.inout.apiserver.base.alias.WordDefinitionId

data class StudyCreateObject(
    val userId: UserId,
    val wordDefinitionId: WordDefinitionId,
)

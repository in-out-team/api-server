package com.inout.apiserver.domain.study

import com.inout.apiserver.infrastructure.db.study.Study
import com.inout.apiserver.infrastructure.db.word.Word

data class StudyWord(
    val study: Study,
    val word: Word,
)

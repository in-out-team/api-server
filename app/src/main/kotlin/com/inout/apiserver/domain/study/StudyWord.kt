package com.inout.apiserver.domain.study

import com.inout.apiserver.domain.word.Word
import com.inout.apiserver.infrastructure.db.study.Study

data class StudyWord(
    val study: Study,
    val word: Word,
)

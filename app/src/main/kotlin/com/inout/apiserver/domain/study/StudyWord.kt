package com.inout.apiserver.domain.study

import com.inout.apiserver.domain.word.Word

data class StudyWord(
    val study: Study,
    val word: Word,
)

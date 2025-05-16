package com.inout.apiserver.domain.study

import com.inout.apiserver.domain.word.WordWithDefinitions
import com.inout.apiserver.infrastructure.mongo.study.Study

data class StudyWord(
    val study: Study,
    val word: WordWithDefinitions,
)

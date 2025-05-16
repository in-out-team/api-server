package com.inout.apiserver.domain.study

import com.inout.apiserver.domain.word.WordWithDefinitions
import com.inout.apiserver.infrastructure.mongo.study.MongoStudy

data class MongoStudyWord(
    val study: MongoStudy,
    val word: WordWithDefinitions,
)

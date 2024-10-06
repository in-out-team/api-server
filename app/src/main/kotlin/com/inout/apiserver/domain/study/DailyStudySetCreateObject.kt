package com.inout.apiserver.domain.study

import java.time.LocalDate

data class DailyStudySetCreateObject(
    val userId: Long,
    val date: LocalDate,
)

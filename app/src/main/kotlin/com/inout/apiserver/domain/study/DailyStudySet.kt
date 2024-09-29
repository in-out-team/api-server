package com.inout.apiserver.domain.study

import java.time.LocalDate

data class DailyStudySet(
    val id: Long,
    val userId: Long,
    val studyIds: List<Long>,
    val date: LocalDate,
)

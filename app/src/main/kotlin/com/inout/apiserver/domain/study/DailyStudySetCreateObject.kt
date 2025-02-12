package com.inout.apiserver.domain.study

import com.inout.apiserver.base.alias.UserId
import java.time.LocalDate

data class DailyStudySetCreateObject(
    val userId: UserId,
    val date: LocalDate,
)

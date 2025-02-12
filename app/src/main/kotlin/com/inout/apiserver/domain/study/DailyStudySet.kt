package com.inout.apiserver.domain.study

import com.inout.apiserver.base.alias.DailyStudySetId
import com.inout.apiserver.base.alias.StudyId
import com.inout.apiserver.base.alias.UserId
import java.time.LocalDate

data class DailyStudySet(
    val id: DailyStudySetId,
    val userId: UserId,
    val studyIds: List<StudyId>,
    val date: LocalDate,
)

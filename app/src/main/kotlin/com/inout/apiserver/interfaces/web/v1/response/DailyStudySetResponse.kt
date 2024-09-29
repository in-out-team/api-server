package com.inout.apiserver.interfaces.web.v1.response

import com.inout.apiserver.domain.study.StudyWord
import java.time.LocalDate

data class DailyStudySetResponse(
    val date: LocalDate,
    val studies: List<StudyWordResponse>,
) {
    companion object {
        fun of(
            date: LocalDate,
            studies: List<StudyWord>,
        ): DailyStudySetResponse {
            return DailyStudySetResponse(
                date = date,
                studies = studies.map { StudyWordResponse.of(study = it.study, word = it.word) },
            )
        }
    }
}

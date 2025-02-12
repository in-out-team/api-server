package com.inout.apiserver.interfaces.web.v1.response

import com.inout.apiserver.base.enums.FsrsCardRating
import com.inout.apiserver.domain.study.StudyWord
import com.inout.apiserver.infrastructure.db.study.DailyStudySet
import java.time.LocalDate

data class DailyStudySetResponse(
    val id: Long,
    val date: LocalDate,
    val studies: List<StudyWordResponse>,
    val extraData: ExtraData,
) {
    companion object {
        fun of(
            dailyStudySet: DailyStudySet,
            studies: List<StudyWord>,
        ): DailyStudySetResponse {
            val studyWordResponses = studies.map { StudyWordResponse.of(study = it.study, word = it.word) }
            val extraData =
                studies.map { it.study.reviewLogs }.fold(ExtraData()) { acc, reviewLogs ->
                    when {
                        reviewLogs.isEmpty() -> acc.copy(newCount = acc.newCount + 1)
                        reviewLogs.last().rating == FsrsCardRating.EASY -> acc.copy(easyCount = acc.easyCount + 1)
                        reviewLogs.last().rating == FsrsCardRating.GOOD -> acc.copy(goodCount = acc.goodCount + 1)
                        reviewLogs.last().rating == FsrsCardRating.HARD -> acc.copy(hardCount = acc.hardCount + 1)
                        reviewLogs.last().rating == FsrsCardRating.AGAIN -> acc.copy(againCount = acc.againCount + 1)
                        else -> acc
                    }
                }
            return DailyStudySetResponse(
                id = dailyStudySet.id!!,
                date = dailyStudySet.date,
                studies = studyWordResponses,
                extraData = extraData,
            )
        }
    }

    data class ExtraData(
        val easyCount: Int = 0,
        val goodCount: Int = 0,
        val hardCount: Int = 0,
        val againCount: Int = 0,
        val newCount: Int = 0,
    )
}

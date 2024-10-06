package com.inout.apiserver.infrastructure.db.study

import com.inout.apiserver.domain.study.DailyStudySet
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
class DailyStudySetRepository(
    private val dailyStudySetJpaRepository: DailyStudySetJpaRepository,
) {
    fun save(dailyStudySet: DailyStudySetEntity): DailyStudySet {
        return dailyStudySetJpaRepository.save(dailyStudySet).toDomain()
    }

    fun findByUserIdAndDate(
        userId: Long,
        date: LocalDate,
    ): DailyStudySet? {
        return dailyStudySetJpaRepository.findByUserIdAndDate(userId, date)?.toDomain()
    }
}

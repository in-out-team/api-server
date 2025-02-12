package com.inout.apiserver.infrastructure.db.study

import com.inout.apiserver.base.alias.DailyStudySetId
import com.inout.apiserver.base.alias.UserId
import com.inout.apiserver.domain.study.DailyStudySet
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
class DailyStudySetRepository(
    private val dailyStudySetJpaRepository: DailyStudySetJpaRepository,
) {
    fun save(dailyStudySet: DailyStudySetEntity): DailyStudySet = dailyStudySetJpaRepository.save(dailyStudySet).toDomain()

    fun findByUserIdAndDate(
        userId: UserId,
        date: LocalDate,
    ): DailyStudySet? = dailyStudySetJpaRepository.findByUserIdAndDate(userId, date)?.toDomain()

    fun findById(id: DailyStudySetId): DailyStudySet? = dailyStudySetJpaRepository.findById(id).map { it.toDomain() }.orElse(null)
}

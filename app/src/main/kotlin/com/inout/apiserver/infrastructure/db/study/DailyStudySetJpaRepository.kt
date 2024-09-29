package com.inout.apiserver.infrastructure.db.study

import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate

interface DailyStudySetJpaRepository : JpaRepository<DailyStudySetEntity, Long> {
    fun findByUserIdAndDate(
        userId: Long,
        date: LocalDate,
    ): DailyStudySetEntity?
}

package com.inout.apiserver.infrastructure.db.study

import com.inout.apiserver.base.alias.DailyStudySetId
import com.inout.apiserver.base.alias.UserId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface DailyStudySetRepository : JpaRepository<DailyStudySet, DailyStudySetId> {
    fun findByUserIdAndDate(
        userId: UserId,
        date: LocalDate,
    ): DailyStudySet?
}

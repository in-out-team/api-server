package com.inout.apiserver.infrastructure.db.study

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import java.time.Instant

interface StudyJpaRepository : JpaRepository<StudyEntity, Long> {
    fun findByUserIdAndWordDefinitionId(
        userId: Long,
        wordDefinitionId: Long,
    ): StudyEntity?

    fun findAllByUserId(
        userId: Long,
        sortIgnoredPageRequest: Pageable,
    ): Page<StudyEntity>

    fun findAllByUserIdAndDueLessThanOrderByDue(
        userId: Long,
        due: Instant,
        pageable: Pageable,
    ): Page<StudyEntity>

    fun findAllByUserIdAndDueLessThanAndIdNotInOrderByDue(
        userId: Long,
        due: Instant,
        excludeIds: List<Long>,
        pageable: Pageable,
    ): Page<StudyEntity>
}

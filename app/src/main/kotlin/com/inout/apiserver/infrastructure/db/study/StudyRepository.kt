package com.inout.apiserver.infrastructure.db.study

import com.inout.apiserver.domain.study.Study
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
class StudyRepository(
    private val studyJpaRepository: StudyJpaRepository,
) {
    fun save(study: StudyEntity): Study {
        return studyJpaRepository.save(study).toDomain()
    }

    fun findByUserIdAndWordDefinitionId(
        userId: Long,
        wordDefinitionId: Long,
    ): Study? {
        return studyJpaRepository.findByUserIdAndWordDefinitionId(userId, wordDefinitionId)?.toDomain()
    }

    fun findById(id: Long): Study? {
        return studyJpaRepository.findById(id).orElse(null)?.toDomain()
    }

    fun findAllByUserId(
        userId: Long,
        pageable: Pageable,
    ): Page<Study> {
        return studyJpaRepository.findAllByUserId(userId, pageable).map { it.toDomain() }
    }

    fun findAllByIds(ids: List<Long>): List<Study> {
        return studyJpaRepository.findAllById(ids).map { it.toDomain() }
    }

    fun findAllPastDueStudiesBy(
        userId: Long,
        due: Instant,
        pageable: Pageable,
    ): Page<Study> {
        return studyJpaRepository.findAllByUserIdAndDueLessThanOrderByDue(userId, due, pageable)
            .map { it.toDomain() }
    }
}

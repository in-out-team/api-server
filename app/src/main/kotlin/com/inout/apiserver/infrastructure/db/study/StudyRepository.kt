package com.inout.apiserver.infrastructure.db.study

import com.inout.apiserver.domain.study.Study
import org.springframework.stereotype.Repository

@Repository
class StudyRepository(
    private val studyJpaRepository: StudyJpaRepository,
) {
    fun save(study: StudyEntity): Study {
        return studyJpaRepository.save(study).toDomain()
    }

    fun findByUserIdAndWordDefinitionId(userId: Long, wordDefinitionId: Long): Study? {
        return studyJpaRepository.findByUserIdAndWordDefinitionId(userId, wordDefinitionId)?.toDomain()
    }
}

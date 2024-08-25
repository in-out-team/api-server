package com.inout.apiserver.infrastructure.db.study

import org.springframework.data.jpa.repository.JpaRepository

interface StudyJpaRepository : JpaRepository<StudyEntity, Long> {
    fun findByUserIdAndWordDefinitionId(userId: Long, wordDefinitionId: Long): StudyEntity?
}
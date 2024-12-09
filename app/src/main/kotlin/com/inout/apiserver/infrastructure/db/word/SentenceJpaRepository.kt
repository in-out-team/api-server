package com.inout.apiserver.infrastructure.db.word

import com.inout.apiserver.base.enums.SentenceType
import org.springframework.data.jpa.repository.JpaRepository

interface SentenceJpaRepository : JpaRepository<SentenceEntity, Long> {
    fun findAllByWordDefinitionId(wordDefinitionId: Long): List<SentenceEntity>

    fun findAllByWordDefinitionIdAndType(
        wordDefinitionId: Long,
        type: SentenceType,
    ): List<SentenceEntity>
}

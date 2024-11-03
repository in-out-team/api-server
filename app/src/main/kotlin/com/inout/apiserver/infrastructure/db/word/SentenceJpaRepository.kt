package com.inout.apiserver.infrastructure.db.word

import org.springframework.data.jpa.repository.JpaRepository

interface SentenceJpaRepository : JpaRepository<SentenceEntity, Long> {
    fun findAllByWordDefinitionId(wordDefinitionId: Long): List<SentenceEntity>
}

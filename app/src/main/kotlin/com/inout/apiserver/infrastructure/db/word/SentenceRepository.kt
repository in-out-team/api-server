package com.inout.apiserver.infrastructure.db.word

import com.inout.apiserver.base.alias.SentenceId
import com.inout.apiserver.base.alias.WordDefinitionId
import com.inout.apiserver.base.enums.SentenceType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface SentenceRepository : JpaRepository<Sentence, SentenceId> {
    fun findAllByWordDefinitionId(wordDefinitionId: WordDefinitionId): List<Sentence>

    fun findAllByWordDefinitionIdAndType(
        wordDefinitionId: WordDefinitionId,
        type: SentenceType,
    ): List<Sentence>
}

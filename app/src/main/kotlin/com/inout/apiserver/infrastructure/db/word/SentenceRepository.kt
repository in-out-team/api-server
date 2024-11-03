package com.inout.apiserver.infrastructure.db.word

import com.inout.apiserver.domain.word.Sentence
import org.springframework.stereotype.Repository

@Repository
class SentenceRepository(
    private val sentenceJpaRepository: SentenceJpaRepository,
) {
    fun save(sentence: SentenceEntity): Sentence {
        return sentenceJpaRepository.save(sentence).toDomain()
    }

    fun findAllByWordDefinitionId(wordDefinitionId: Long): List<Sentence> {
        return sentenceJpaRepository.findAllByWordDefinitionId(wordDefinitionId).map { it.toDomain() }
    }
}

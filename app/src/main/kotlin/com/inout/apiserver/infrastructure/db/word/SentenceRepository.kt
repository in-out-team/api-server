package com.inout.apiserver.infrastructure.db.word

import com.inout.apiserver.base.enums.SentenceType
import com.inout.apiserver.domain.word.Sentence
import org.springframework.stereotype.Repository

@Repository
class SentenceRepository(
    private val sentenceJpaRepository: SentenceJpaRepository,
) {
    fun save(sentence: SentenceEntity): Sentence = sentenceJpaRepository.save(sentence).toDomain()

    fun findAllByWordDefinitionId(wordDefinitionId: Long): List<Sentence> =
        sentenceJpaRepository.findAllByWordDefinitionId(wordDefinitionId).map {
            it.toDomain()
        }

    fun findById(id: Long): Sentence? = sentenceJpaRepository.findById(id).map { it.toDomain() }.orElse(null)

    fun findAllByWordDefinitionIdAndType(
        wordDefinitionId: Long,
        type: SentenceType,
    ): List<Sentence> = sentenceJpaRepository.findAllByWordDefinitionIdAndType(wordDefinitionId, type).map { it.toDomain() }
}

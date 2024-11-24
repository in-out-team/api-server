package com.inout.apiserver.infrastructure.db.word

import com.inout.apiserver.domain.word.Sentence
import com.inout.apiserver.domain.word.SentenceCreateObject
import com.inout.apiserver.error.InOutRequireNotNullException
import com.inout.apiserver.infrastructure.db.BaseEntity
import jakarta.persistence.Entity
import jakarta.persistence.Index
import jakarta.persistence.Table

@Entity
@Table(
    name = "sentences",
    indexes = [
        Index(columnList = "word_definition_id", name = "idx_sentences_word_definition_id"),
    ],
)
data class SentenceEntity(
    val wordDefinitionId: Long,
    val content: String,
    val translation: String,
) : BaseEntity() {
    fun toDomain(): Sentence =
        Sentence(
            id = id ?: throw InOutRequireNotNullException("Sentence id is null", "IORNN_SENTENCE_1"),
            wordDefinitionId = wordDefinitionId,
            content = content,
            translation = translation,
        )

    companion object {
        fun of(sentence: Sentence): SentenceEntity =
            SentenceEntity(
                wordDefinitionId = sentence.wordDefinitionId,
                content = sentence.content,
                translation = sentence.translation,
            ).apply {
                id = sentence.id
            }

        fun fromCreateObject(sentenceCreateObject: SentenceCreateObject): SentenceEntity =
            SentenceEntity(
                wordDefinitionId = sentenceCreateObject.wordDefinitionId,
                content = sentenceCreateObject.content,
                translation = sentenceCreateObject.translation,
            )
    }
}

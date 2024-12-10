package com.inout.apiserver.infrastructure.db.word

import com.inout.apiserver.base.enums.LexicalCategoryType
import com.inout.apiserver.base.enums.SentenceType
import com.inout.apiserver.domain.word.Sentence
import com.inout.apiserver.domain.word.SentenceCreateObject
import com.inout.apiserver.error.InOutRequireNotNullException
import com.inout.apiserver.infrastructure.db.BaseEntity
import io.hypersistence.utils.hibernate.type.json.JsonType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Index
import jakarta.persistence.Table
import org.hibernate.annotations.Type

@Entity
@Table(
    name = "sentences",
    indexes = [
        Index(columnList = "word_definition_id", name = "idx_sentences_word_definition_id"),
    ],
)
data class SentenceEntity(
    val wordDefinitionId: Long,
    val type: SentenceType,
    val content: String,
    val translation: String,
    @Type(JsonType::class)
    @Column(columnDefinition = "jsonb")
    val lexicalCategories: List<LexicalCategoryInfo>,
) : BaseEntity() {
    data class LexicalCategoryInfo(
        val word: String,
        val lexicalCategory: LexicalCategoryType,
    )

    fun toDomain(): Sentence =
        Sentence(
            id = id ?: throw InOutRequireNotNullException("Sentence id is null", "IORNN_SENTENCE_1"),
            wordDefinitionId = wordDefinitionId,
            type = type,
            content = content,
            translation = translation,
            lexicalCategories = lexicalCategories,
        )

    companion object {
        fun fromCreateObject(sentenceCreateObject: SentenceCreateObject): SentenceEntity =
            SentenceEntity(
                wordDefinitionId = sentenceCreateObject.wordDefinitionId,
                type = sentenceCreateObject.type,
                content = sentenceCreateObject.content,
                translation = sentenceCreateObject.translation,
                lexicalCategories = sentenceCreateObject.lexicalCategories,
            )
    }
}

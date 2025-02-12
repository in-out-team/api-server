package com.inout.apiserver.infrastructure.db.word

import com.inout.apiserver.base.alias.SentenceId
import com.inout.apiserver.base.alias.WordDefinitionId
import com.inout.apiserver.base.enums.LexicalCategoryType
import com.inout.apiserver.base.enums.SentenceType
import com.inout.apiserver.domain.word.SentenceCreateObject
import com.inout.apiserver.infrastructure.db.TimestampedEntity
import io.hypersistence.utils.hibernate.type.json.JsonType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
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
data class Sentence(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: SentenceId? = null,
    val wordDefinitionId: WordDefinitionId,
    @Enumerated(EnumType.STRING)
    val type: SentenceType,
    val content: String,
    val translation: String,
    @Type(JsonType::class)
    @Column(columnDefinition = "jsonb")
    val lexicalCategories: List<LexicalCategoryInfo>,
) : TimestampedEntity() {
    data class LexicalCategoryInfo(
        val word: String,
        val lexicalCategory: LexicalCategoryType,
    )

    companion object {
        fun fromCreateObject(createObject: SentenceCreateObject): Sentence =
            Sentence(
                wordDefinitionId = createObject.wordDefinitionId,
                type = createObject.type,
                content = createObject.content,
                translation = createObject.translation,
                lexicalCategories = createObject.lexicalCategories,
            )
    }
}

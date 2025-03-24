package com.inout.apiserver.infrastructure.db.word

import com.inout.apiserver.base.alias.WordDefinitionId
import com.inout.apiserver.base.enums.LexicalCategoryType
import com.inout.apiserver.domain.word.WordDefinitionCreateObject
import com.inout.apiserver.infrastructure.db.TimestampedEntity
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "word_definitions")
data class WordDefinition(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: WordDefinitionId? = null,
    @ManyToOne
    @JoinColumn(name = "word_id")
    val word: Word,
    @Enumerated(EnumType.STRING)
    val lexicalCategory: LexicalCategoryType,
    val meaning: String,
    val preContext: String,
) : TimestampedEntity() {
    companion object {
        fun fromCreateObject(
            createObject: WordDefinitionCreateObject,
            word: Word,
        ): WordDefinition =
            WordDefinition(
                word = word,
                lexicalCategory = createObject.lexicalCategory,
                meaning = createObject.meaning,
                preContext = createObject.preContext,
            )
    }

    override fun toString(): String =
        "WordDefinition(id=$id, wordId=${word.id}, lexicalCategory=$lexicalCategory, meaning='$meaning', preContext='$preContext')"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as WordDefinition

        if (id != other.id) return false
        if (word.id != other.word.id) return false
        if (lexicalCategory != other.lexicalCategory) return false
        if (meaning != other.meaning) return false
        if (preContext != other.preContext) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + word.hashCode()
        result = 31 * result + lexicalCategory.hashCode()
        result = 31 * result + meaning.hashCode()
        result = 31 * result + preContext.hashCode()
        return result
    }
}

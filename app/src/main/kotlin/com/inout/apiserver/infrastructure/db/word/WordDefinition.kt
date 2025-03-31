package com.inout.apiserver.infrastructure.db.word

import com.inout.apiserver.base.alias.WordDefinitionId
import com.inout.apiserver.base.alias.WordId
import com.inout.apiserver.base.enums.LexicalCategoryType
import com.inout.apiserver.base.enums.StatusType
import com.inout.apiserver.domain.word.WordDefinitionCreateObject
import com.inout.apiserver.infrastructure.db.TimestampedEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "word_definitions")
data class WordDefinition(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: WordDefinitionId? = null,
    @Column(nullable = false, name = "word_id")
    val wordId: WordId,
    @Enumerated(EnumType.STRING)
    val lexicalCategory: LexicalCategoryType,
    val meaning: String,
    val preContext: String,
    @Enumerated(EnumType.STRING)
    val status: StatusType = StatusType.PENDING,
) : TimestampedEntity() {
    companion object {
        fun fromCreateObject(
            createObject: WordDefinitionCreateObject,
            word: Word,
        ): WordDefinition =
            WordDefinition(
                wordId = word.id!!,
                lexicalCategory = createObject.lexicalCategory,
                meaning = createObject.meaning,
                preContext = createObject.preContext,
                status = StatusType.PENDING,
            )
    }
}

package com.inout.apiserver.infrastructure.db.word

import com.inout.apiserver.base.enums.LexicalCategoryType
import com.inout.apiserver.domain.word.WordDefinition
import com.inout.apiserver.domain.word.WordDefinitionCreateObject
import com.inout.apiserver.error.InOutRequireNotNullException
import com.inout.apiserver.infrastructure.db.BaseEntity
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Table

@Entity
@Table(name = "word_definitions")
data class WordDefinitionEntity(
    @Enumerated(EnumType.STRING)
    val lexicalCategory: LexicalCategoryType,
    val meaning: String,
    val preContext: String
) : BaseEntity() {
    fun toDomain(): WordDefinition {
        return WordDefinition(
            id = id ?: throw InOutRequireNotNullException("WordDefinition id is null", "IORNN_WORDDEF_1"),
            lexicalCategory = lexicalCategory,
            meaning = meaning,
            preContext = preContext,
        )
    }

    companion object {
        fun of(wordDefinition: WordDefinition): WordDefinitionEntity {
            return WordDefinitionEntity(
                lexicalCategory = wordDefinition.lexicalCategory,
                meaning = wordDefinition.meaning,
                preContext = wordDefinition.preContext
            ).apply {
                id = wordDefinition.id
            }
        }

        fun fromCreateObject(wordDefinitionCreateObject: WordDefinitionCreateObject): WordDefinitionEntity {
            return WordDefinitionEntity(
                lexicalCategory = wordDefinitionCreateObject.lexicalCategory,
                meaning = wordDefinitionCreateObject.meaning,
                preContext = wordDefinitionCreateObject.preContext
            )
        }
    }
}

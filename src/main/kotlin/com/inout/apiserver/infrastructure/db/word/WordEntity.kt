package com.inout.apiserver.infrastructure.db.word

import com.inout.apiserver.base.enums.LanguageType
import com.inout.apiserver.domain.word.Word
import com.inout.apiserver.domain.word.WordCreateObject
import com.inout.apiserver.error.InOutRequireNotNullException
import com.inout.apiserver.infrastructure.db.BaseEntity
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import org.hibernate.annotations.DynamicUpdate

@Entity
@Table(name = "words", uniqueConstraints = [
    UniqueConstraint(columnNames = ["name", "language"])
])
@DynamicUpdate
data class WordEntity(
    val name: String,
    @Enumerated(EnumType.STRING)
    val language: LanguageType = LanguageType.ENGLISH
) : BaseEntity() {
    fun toDomain(): Word {
        return Word(
            id = id ?: throw InOutRequireNotNullException("Word id is null", "IORNN_WORD_1"),
            name = name,
            language = language,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
    }

    companion object {
        fun of(word: Word): WordEntity {
            return WordEntity(
                name = word.name,
                language = word.language
            ).apply {
                id = word.id
                createdAt = word.createdAt
                updatedAt = word.updatedAt
            }
        }

        fun fromCreateObject(wordCreateObject: WordCreateObject): WordEntity {
            return WordEntity(
                name = wordCreateObject.name,
                language = wordCreateObject.language
            )
        }
    }
}

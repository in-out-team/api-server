package com.inout.apiserver.infrastructure.db.word

import com.inout.apiserver.base.enums.LanguageType
import com.inout.apiserver.domain.word.Word
import com.inout.apiserver.domain.word.WordCreateObject
import com.inout.apiserver.error.InOutRequireNotNullException
import com.inout.apiserver.infrastructure.db.BaseEntity
import jakarta.persistence.*
import org.hibernate.annotations.DynamicUpdate

@Entity
@Table(name = "words", uniqueConstraints = [
    UniqueConstraint(columnNames = ["name", "from_language", "to_language"])
])
@DynamicUpdate
data class WordEntity(
    val name: String,
    @Enumerated(EnumType.STRING)
    val fromLanguage: LanguageType,
    @Enumerated(EnumType.STRING)
    val toLanguage: LanguageType,
    @OneToMany(cascade = [CascadeType.ALL])
    @JoinColumn(name = "word_id")
    val definitions: List<WordDefinitionEntity>,
) : BaseEntity() {
    fun toDomain(): Word {
        return Word(
            id = id ?: throw InOutRequireNotNullException("Word id is null", "IORNN_WORD_1"),
            name = name,
            fromLanguage = fromLanguage,
            toLanguage = toLanguage,
            definitions = definitions.map { it.toDomain() },
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
    }

    companion object {
        fun of(word: Word): WordEntity {
            return WordEntity(
                name = word.name,
                fromLanguage = word.fromLanguage,
                toLanguage = word.toLanguage,
                definitions = word.definitions.map { WordDefinitionEntity.of(it) }
            ).apply {
                id = word.id
                createdAt = word.createdAt
                updatedAt = word.updatedAt
            }
        }

        fun fromCreateObject(wordCreateObject: WordCreateObject): WordEntity {
            return WordEntity(
                name = wordCreateObject.name,
                fromLanguage = wordCreateObject.fromLanguage,
                toLanguage = wordCreateObject.toLanguage,
                definitions = wordCreateObject.definitions.map { WordDefinitionEntity.fromCreateObject(it) }
            )
        }
    }
}

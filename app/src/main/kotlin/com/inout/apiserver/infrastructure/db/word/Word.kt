package com.inout.apiserver.infrastructure.db.word

import com.google.common.collect.Iterables
import com.inout.apiserver.base.alias.WordId
import com.inout.apiserver.base.enums.LanguageType
import com.inout.apiserver.domain.word.WordCreateObject
import com.inout.apiserver.infrastructure.db.TimestampedEntity
import jakarta.persistence.CascadeType
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import org.hibernate.annotations.DynamicInsert
import org.hibernate.annotations.DynamicUpdate

@Entity
@Table(
    name = "words",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["name", "from_language", "to_language"]),
    ],
)
@DynamicUpdate
@DynamicInsert
data class Word(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: WordId? = null,
    val name: String,
    @Enumerated(EnumType.STRING)
    val fromLanguage: LanguageType,
    @Enumerated(EnumType.STRING)
    val toLanguage: LanguageType,
    @OneToMany(cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
    @JoinColumn(name = "word_id")
    val definitions: MutableList<WordDefinition> = mutableListOf(),
) : TimestampedEntity() {
    companion object {
        fun fromCreateObject(createObject: WordCreateObject): Word =
            Word(
                name = createObject.name,
                fromLanguage = createObject.fromLanguage,
                toLanguage = createObject.toLanguage,
            )
    }

    fun addDefinitions(definitions: List<WordDefinition>): Word {
        this.definitions.addAll(definitions)

        return this
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Word

        if (id != other.id) return false
        if (name != other.name) return false
        if (fromLanguage != other.fromLanguage) return false
        if (toLanguage != other.toLanguage) return false
        // Use Iterables.elementsEqual to compare two lists of reviewLogs because reviewLogs may be a proxy object
        if (!Iterables.elementsEqual(definitions, other.definitions)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + name.hashCode()
        result = 31 * result + fromLanguage.hashCode()
        result = 31 * result + toLanguage.hashCode()
        result = 31 * result + definitions.toList().hashCode()

        return result
    }
}

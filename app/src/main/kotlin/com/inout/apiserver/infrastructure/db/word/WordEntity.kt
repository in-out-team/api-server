package com.inout.apiserver.infrastructure.db.word

import com.inout.apiserver.base.enums.LanguageType
import com.inout.apiserver.domain.word.Word
import com.inout.apiserver.domain.word.WordCreateObject
import com.inout.apiserver.error.InOutRequireNotNullException
import com.inout.apiserver.infrastructure.db.BaseEntity
import jakarta.persistence.CascadeType
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import org.hibernate.annotations.DynamicUpdate

@Entity
@Table(
    name = "words",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["name", "from_language", "to_language"]),
    ],
)
@DynamicUpdate
data class WordEntity(
    val name: String,
    @Enumerated(EnumType.STRING)
    val fromLanguage: LanguageType,
    @Enumerated(EnumType.STRING)
    val toLanguage: LanguageType,
    /**
     * FIXME:
     * - using eager fetch is temporary solution for now to solve the below issue:
     * ```
     * org.springframework.orm.jpa.JpaSystemException: failed to lazily initialize a collection of role:
     * com.inout.apiserver.infrastructure.db.study.StudyEntity.reviewLogs: could not initialize proxy - no Session
     * ```
     * check sources:
     * - https://stackoverflow.com/questions/11746499/how-to-solve-the-failed-to-lazily-initialize-a-collection-of-role-hibernate-ex
     * -----------------------
     * in here, toDomain() triggers reviewLogs.map { it.toDomain() } which is a lazy loading
     */
    @OneToMany(cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
    @JoinColumn(name = "word_id")
    val definitions: List<WordDefinitionEntity>,
) : BaseEntity() {
    fun toDomain(): Word =
        Word(
            id = id ?: throw InOutRequireNotNullException("Word id is null", "IORNN_WORD_1"),
            name = name,
            fromLanguage = fromLanguage,
            toLanguage = toLanguage,
            definitions = definitions.map { it.toDomain() },
            createdAt = createdAt,
            updatedAt = updatedAt,
        )

    companion object {
        fun fromDomain(word: Word): WordEntity =
            WordEntity(
                name = word.name,
                fromLanguage = word.fromLanguage,
                toLanguage = word.toLanguage,
                definitions = word.definitions.map { WordDefinitionEntity.of(it) },
            ).apply {
                id = word.id
                createdAt = word.createdAt
                updatedAt = word.updatedAt
            }

        fun fromCreateObject(wordCreateObject: WordCreateObject): WordEntity =
            WordEntity(
                name = wordCreateObject.name,
                fromLanguage = wordCreateObject.fromLanguage,
                toLanguage = wordCreateObject.toLanguage,
                definitions = wordCreateObject.definitions.map { WordDefinitionEntity.fromCreateObject(it) },
            )
    }
}

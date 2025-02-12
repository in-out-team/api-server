package com.inout.apiserver.infrastructure.db.word

import com.inout.apiserver.base.enums.LanguageType
import com.inout.apiserver.base.enums.LexicalCategoryType
import jakarta.persistence.criteria.JoinType
import org.springframework.data.jpa.domain.Specification

class WordSpecification {
    companion object {
        fun fromLanguage(fromLanguage: LanguageType): Specification<com.inout.apiserver.infrastructure.db.word.Word> =
            Specification { root, _, criteriaBuilder ->
                criteriaBuilder.equal(root.get<LanguageType>("fromLanguage"), fromLanguage)
            }

        fun toLanguage(toLanguage: LanguageType): Specification<com.inout.apiserver.infrastructure.db.word.Word> =
            Specification { root, _, criteriaBuilder ->
                criteriaBuilder.equal(root.get<LanguageType>("toLanguage"), toLanguage)
            }

        fun prefix(prefix: String): Specification<com.inout.apiserver.infrastructure.db.word.Word> =
            Specification { root, _, criteriaBuilder ->
                criteriaBuilder.like(root.get("name"), "$prefix%")
            }

        fun lexicalCategoryType(lexicalCategoryType: LexicalCategoryType?): Specification<com.inout.apiserver.infrastructure.db.word.Word> {
            return Specification { root, query, criteriaBuilder ->
                if (lexicalCategoryType == null) {
                    return@Specification criteriaBuilder.conjunction()
                }
                val definitions = root.join<Word, WordDefinition>("definitions", JoinType.INNER)
                query.distinct(true)
                criteriaBuilder.equal(definitions.get<LexicalCategoryType>("lexicalCategory"), lexicalCategoryType)
            }
        }
    }
}

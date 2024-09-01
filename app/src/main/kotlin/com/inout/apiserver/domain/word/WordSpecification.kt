package com.inout.apiserver.domain.word

import com.inout.apiserver.base.enums.LanguageType
import com.inout.apiserver.base.enums.LexicalCategoryType
import com.inout.apiserver.infrastructure.db.word.WordDefinitionEntity
import com.inout.apiserver.infrastructure.db.word.WordEntity
import jakarta.persistence.criteria.JoinType
import org.springframework.data.jpa.domain.Specification

class WordSpecification {
    companion object {
        fun fromLanguage(fromLanguage: LanguageType): Specification<WordEntity> {
            return Specification { root, _, criteriaBuilder ->
                criteriaBuilder.equal(root.get<LanguageType>("fromLanguage"), fromLanguage)
            }
        }

        fun toLanguage(toLanguage: LanguageType): Specification<WordEntity> {
            return Specification { root, _, criteriaBuilder ->
                criteriaBuilder.equal(root.get<LanguageType>("toLanguage"), toLanguage)
            }
        }

        fun prefix(prefix: String): Specification<WordEntity> {
            return Specification { root, _, criteriaBuilder ->
                criteriaBuilder.like(root.get("name"), "$prefix%")
            }
        }

        fun lexicalCategoryType(lexicalCategoryType: LexicalCategoryType?): Specification<WordEntity> {
            return Specification { root, query, criteriaBuilder ->
                if (lexicalCategoryType == null) {
                    return@Specification criteriaBuilder.conjunction()
                }
                val definitions = root.join<WordEntity, WordDefinitionEntity>("definitions", JoinType.INNER)
                query.distinct(true)
                criteriaBuilder.equal(definitions.get<LexicalCategoryType>("lexicalCategory"), lexicalCategoryType)
            }
        }
    }
}

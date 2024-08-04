package com.inout.apiserver.infrastructure.db.word

import com.inout.apiserver.base.enums.LanguageType
import com.inout.apiserver.base.enums.LexicalCategoryType
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
            return Specification { root, _, criteriaBuilder ->
                if (lexicalCategoryType == null) {
                    return@Specification criteriaBuilder.conjunction()
                }
                val definitions = root.join<WordEntity, WordDefinitionEntity>("definitions", JoinType.INNER)
                criteriaBuilder.equal(definitions.get<LexicalCategoryType>("lexicalCategory"), lexicalCategoryType)
            }
        }
    }
}
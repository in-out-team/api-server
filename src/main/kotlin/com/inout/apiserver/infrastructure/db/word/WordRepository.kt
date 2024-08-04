package com.inout.apiserver.infrastructure.db.word

import com.inout.apiserver.base.enums.LanguageType
import com.inout.apiserver.base.enums.LexicalCategoryType
import com.inout.apiserver.domain.word.Word
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Repository

@Repository
class WordRepository(
    private val wordJpaRepository: WordJpaRepository
) {
    fun save(word: WordEntity): Word {
        return wordJpaRepository.save(word).toDomain()
    }

    fun findByNameAndFromLanguageAndToLanguage(name: String, fromLanguage: LanguageType, toLanguage: LanguageType): Word? {
        return wordJpaRepository.findByNameAndFromLanguageAndToLanguage(name, fromLanguage, toLanguage)?.toDomain()
    }

    fun findById(id: Long): Word? {
        return wordJpaRepository.findById(id).orElse(null)?.toDomain()
    }

    fun findWordsWithDefinitions(
        fromLanguage: LanguageType,
        toLanguage: LanguageType,
        prefix: String,
        lexicalCategoryType: LexicalCategoryType?,
        pageable: Pageable
    ): Page<Word> {
        val spec = Specification.where(WordSpecification.fromLanguage(fromLanguage))
            .and(WordSpecification.toLanguage(toLanguage))
            .and(WordSpecification.prefix(prefix))
            .and(WordSpecification.lexicalCategoryType(lexicalCategoryType))

        return wordJpaRepository.findAll(spec, pageable).map { it.toDomain() }
    }
}

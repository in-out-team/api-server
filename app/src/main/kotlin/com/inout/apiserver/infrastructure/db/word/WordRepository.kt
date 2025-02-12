package com.inout.apiserver.infrastructure.db.word

import com.inout.apiserver.base.alias.WordDefinitionId
import com.inout.apiserver.base.alias.WordId
import com.inout.apiserver.base.enums.LanguageType
import com.inout.apiserver.base.enums.LexicalCategoryType
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.stereotype.Repository

interface WordJpaRepository :
    JpaRepository<Word, WordId>,
    JpaSpecificationExecutor<Word> {
    fun findByNameAndFromLanguageAndToLanguage(
        name: String,
        fromLanguage: LanguageType,
        toLanguage: LanguageType,
    ): Word?

    fun findByDefinitionsId(wordDefinitionId: WordDefinitionId): Word?

    fun findAllByDefinitionsIdIn(wordDefinitionIds: List<WordDefinitionId>): List<Word>
}

@Repository
class WordRepository(
    private val wordJpaRepository: WordJpaRepository,
) : WordJpaRepository by wordJpaRepository {
    fun findWordsWithDefinitions(
        fromLanguage: LanguageType,
        toLanguage: LanguageType,
        prefix: String,
        lexicalCategory: LexicalCategoryType?,
        pageable: Pageable,
    ): Page<Word> {
        val spec =
            Specification
                .where(WordSpecification.fromLanguage(fromLanguage))
                .and(WordSpecification.toLanguage(toLanguage))
                .and(WordSpecification.prefix(prefix))
                .and(WordSpecification.lexicalCategoryType(lexicalCategory))

        return wordJpaRepository.findAll(spec, pageable)
    }

    fun findByWordDefinitionId(wordDefinitionId: WordDefinitionId): Word? = wordJpaRepository.findByDefinitionsId(wordDefinitionId)

    fun findAllByWordDefinitionIds(wordDefinitionIds: List<WordDefinitionId>): List<Word> =
        wordJpaRepository.findAllByDefinitionsIdIn(wordDefinitionIds)
}
